package lk.jiat.smartcity360.complaint.dao;

import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.auth.UserRole;
import lk.jiat.smartcity360.complaint.model.ComplaintRecord;
import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.model.EscalationRecord;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;
import lk.jiat.smartcity360.complaint.chain.ComplaintEscalation;
import lk.jiat.smartcity360.config.ConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class JdbcComplaintRepository implements ComplaintRepository {
    @Override
    public long create(String reference, String description, String severity, HandlerLevel handler,
                       long residentId, Long assignedUserId) {
        String insertComplaint = "INSERT INTO complaint " +
                "(reference_no, description, severity, current_handler, resident_id, assigned_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String insertHistory = "INSERT INTO complaint_escalation " +
                "(complaint_id, handler_level, action, note, actor_user_id, actor_role) " +
                "VALUES (?, ?, 'SUBMITTED', 'Complaint received', ?, 'RESIDENT')";
        try (Connection connection = ConnectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement complaint = connection.prepareStatement(insertComplaint, Statement.RETURN_GENERATED_KEYS)) {
                complaint.setString(1, reference);
                complaint.setString(2, description);
                complaint.setString(3, severity);
                complaint.setString(4, handler.name());
                complaint.setLong(5, residentId);
                if (assignedUserId == null) {
                    complaint.setNull(6, java.sql.Types.BIGINT);
                } else {
                    complaint.setLong(6, assignedUserId);
                }
                complaint.executeUpdate();
                long id;
                try (ResultSet keys = complaint.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("The database did not return a complaint ID");
                    }
                    id = keys.getLong(1);
                }
                try (PreparedStatement history = connection.prepareStatement(insertHistory)) {
                    history.setLong(1, id);
                    history.setString(2, handler.name());
                    history.setLong(3, residentId);
                    history.executeUpdate();
                }
                connection.commit();
                return id;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to submit complaint", exception);
        }
    }

    @Override
    public List<ComplaintRecord> findAll() {
        String sql = "SELECT id, reference_no, description, severity, status, current_handler, created_at, " +
                "resident_id, assigned_user_id " +
                "FROM complaint ORDER BY created_at DESC";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<ComplaintRecord> complaints = new ArrayList<>();
            while (rows.next()) {
                complaints.add(readComplaint(rows));
            }
            return complaints;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load complaints", exception);
        }
    }

    @Override
    public List<ComplaintRecord> findAllForResident(long residentId) {
        String sql = "SELECT id, reference_no, description, severity, status, current_handler, created_at, " +
                "resident_id, assigned_user_id FROM complaint WHERE resident_id = ? ORDER BY created_at DESC";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, residentId);
            try (ResultSet rows = statement.executeQuery()) {
                List<ComplaintRecord> complaints = new ArrayList<>();
                while (rows.next()) {
                    complaints.add(readComplaint(rows));
                }
                return complaints;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load resident complaints", exception);
        }
    }

    @Override
    public ComplaintRecord find(long complaintId) {
        String sql = "SELECT id, reference_no, description, severity, status, current_handler, created_at, " +
                "resident_id, assigned_user_id " +
                "FROM complaint WHERE id = ?";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, complaintId);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) {
                    throw new IllegalArgumentException("Complaint does not exist");
                }
                return readComplaint(row);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load complaint", exception);
        }
    }

    @Override
    public List<EscalationRecord> history(long complaintId) {
        String sql = "SELECT handler_level, action, note, occurred_at, actor_user_id, actor_role " +
                "FROM complaint_escalation " +
                "WHERE complaint_id = ? ORDER BY occurred_at, id";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, complaintId);
            try (ResultSet rows = statement.executeQuery()) {
                List<EscalationRecord> history = new ArrayList<>();
                while (rows.next()) {
                    history.add(new EscalationRecord(HandlerLevel.valueOf(rows.getString("handler_level")),
                            rows.getString("action"), rows.getString("note"),
                            rows.getTimestamp("occurred_at").toInstant(),
                            rows.getObject("actor_user_id", Long.class),
                            rows.getString("actor_role") == null ? null
                                    : UserRole.valueOf(rows.getString("actor_role"))));
                }
                return history;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load complaint history", exception);
        }
    }

    @Override
    public void recordEscalation(long complaintId, ComplaintEscalation escalation, String note,
                                 Long assignedUserId, AuthenticatedUser actor) {
        if (escalation == null || escalation.from() == null
                || (escalation.handled() && escalation.to() == null)
                || (!escalation.handled() && !"UNHANDLED".equals(escalation.action()))) {
            throw new IllegalArgumentException("Complaint escalation outcome is invalid");
        }
        HandlerLevel nextHandler = escalation.handled() ? escalation.to() : escalation.from();
        String update = "UPDATE complaint SET current_handler = ?, status = ?, assigned_user_id = ? " +
                "WHERE id = ? AND current_handler = ? AND status <> 'RESOLVED' " +
                "AND (assigned_user_id IS NULL OR assigned_user_id = ? OR assigned_user_id IN " +
                "(SELECT id FROM app_user WHERE active = FALSE OR role <> ?))";
        String history = "INSERT INTO complaint_escalation " +
                "(complaint_id, handler_level, action, note, actor_user_id, actor_role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = ConnectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement updateStatement = connection.prepareStatement(update)) {
                updateStatement.setString(1, nextHandler.name());
                updateStatement.setString(2, "IN_PROGRESS");
                if (assignedUserId == null) {
                    updateStatement.setNull(3, java.sql.Types.BIGINT);
                } else {
                    updateStatement.setLong(3, assignedUserId);
                }
                updateStatement.setLong(4, complaintId);
                updateStatement.setString(5, escalation.from().name());
                updateStatement.setLong(6, actor.id());
                updateStatement.setString(7, actor.role().name());
                if (updateStatement.executeUpdate() != 1) {
                    throw new IllegalStateException("Complaint changed or was resolved; refresh before escalating");
                }
                try (PreparedStatement historyStatement = connection.prepareStatement(history)) {
                    historyStatement.setLong(1, complaintId);
                    historyStatement.setString(2, nextHandler.name());
                    historyStatement.setString(3, escalation.action());
                    historyStatement.setString(4, note);
                    historyStatement.setLong(5, actor.id());
                    historyStatement.setString(6, actor.role().name());
                    historyStatement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | IllegalStateException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to escalate complaint", exception);
        }
    }

    @Override
    public void resolve(long complaintId, HandlerLevel current, AuthenticatedUser actor) {
        String update = "UPDATE complaint SET status = 'RESOLVED' WHERE id = ? " +
                "AND current_handler = ? AND status <> 'RESOLVED' " +
                "AND (assigned_user_id IS NULL OR assigned_user_id = ? OR assigned_user_id IN " +
                "(SELECT id FROM app_user WHERE active = FALSE OR role <> ?))";
        String history = "INSERT INTO complaint_escalation " +
                "(complaint_id, handler_level, action, note, actor_user_id, actor_role) " +
                "VALUES (?, ?, 'RESOLVED', 'Marked resolved', ?, ?)";
        try (Connection connection = ConnectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement updateStatement = connection.prepareStatement(update)) {
                updateStatement.setLong(1, complaintId);
                updateStatement.setString(2, current.name());
                updateStatement.setLong(3, actor.id());
                updateStatement.setString(4, actor.role().name());
                if (updateStatement.executeUpdate() != 1) {
                    throw new IllegalStateException("Complaint changed or was resolved; refresh before updating");
                }
                try (PreparedStatement historyStatement = connection.prepareStatement(history)) {
                    historyStatement.setLong(1, complaintId);
                    historyStatement.setString(2, current.name());
                    historyStatement.setLong(3, actor.id());
                    historyStatement.setString(4, actor.role().name());
                    historyStatement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | IllegalStateException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to resolve complaint", exception);
        }
    }

    private ComplaintRecord readComplaint(ResultSet row) throws SQLException {
        return new ComplaintRecord(row.getLong("id"), row.getString("reference_no"),
                row.getString("description"), ComplaintSeverity.valueOf(row.getString("severity")),
                row.getString("status"), HandlerLevel.valueOf(row.getString("current_handler")),
                row.getTimestamp("created_at").toInstant(), row.getObject("resident_id", Long.class),
                row.getObject("assigned_user_id", Long.class));
    }

    private void rollback(Connection connection, Exception originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
