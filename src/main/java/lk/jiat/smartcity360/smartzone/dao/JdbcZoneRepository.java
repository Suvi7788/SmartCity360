package lk.jiat.smartcity360.smartzone.dao;

import lk.jiat.smartcity360.config.ConnectionProvider;
import lk.jiat.smartcity360.smartzone.composite.ZoneOperation;
import lk.jiat.smartcity360.smartzone.model.ZoneRecord;
import lk.jiat.smartcity360.smartzone.model.ZoneType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class JdbcZoneRepository implements ZoneRepository {
    @Override
    public List<ZoneRecord> findAll() {
        String sql = "SELECT id, parent_id, zone_type, name, power_on, usage_kwh FROM smart_zone ORDER BY name";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<ZoneRecord> zones = new ArrayList<>();
            while (rows.next()) {
                long parent = rows.getLong("parent_id");
                Long parentId = rows.wasNull() ? null : parent;
                zones.add(new ZoneRecord(rows.getLong("id"), parentId,
                        ZoneType.valueOf(rows.getString("zone_type")), rows.getString("name"),
                        rows.getBoolean("power_on"), rows.getBigDecimal("usage_kwh")));
            }
            return zones;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load smart zones", exception);
        }
    }

    @Override
    public long add(Long parentId, String type, String name, BigDecimal usageKwh) {
        ZoneType childType = ZoneType.from(type);
        String cleanName = name == null ? "" : name.trim();
        BigDecimal cleanUsage = usageKwh == null ? BigDecimal.ZERO : usageKwh;
        if (cleanName.isEmpty() || cleanName.length() > 120) {
            throw new IllegalArgumentException("Zone name must contain 1-120 characters");
        }
        if (cleanUsage.signum() < 0 || cleanUsage.scale() > 3
                || cleanUsage.compareTo(new BigDecimal("999999999.999")) > 0) {
            throw new IllegalArgumentException("Usage must be between 0 and 999999999.999 kWh");
        }
        if (parentId == null) {
            if (childType != ZoneType.CITY || cleanUsage.signum() != 0) {
                throw new IllegalArgumentException("Only a city can be created without a parent");
            }
        } else {
            if (parentId < 1) {
                throw new IllegalArgumentException("Parent zone must be a positive number");
            }
            if (childType == ZoneType.CITY || (childType != ZoneType.BUILDING && cleanUsage.signum() != 0)) {
                throw new IllegalArgumentException("Child type or usage does not match the zone hierarchy");
            }
        }
        String sql = "INSERT INTO smart_zone (parent_id, zone_type, name, usage_kwh) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (parentId != null) {
                    validateParentAndChild(connection, parentId, childType);
                }
                ensureSiblingNameAvailable(connection, parentId, cleanName);
                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    if (parentId == null) {
                        statement.setNull(1, java.sql.Types.BIGINT);
                    } else {
                        statement.setLong(1, parentId);
                    }
                    statement.setString(2, childType.name());
                    statement.setString(3, cleanName);
                    statement.setBigDecimal(4, cleanUsage);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("The database did not return a zone ID");
                        }
                        long id = keys.getLong(1);
                        connection.commit();
                        return id;
                    }
                }
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            if (exception.getSQLState() != null && exception.getSQLState().startsWith("23")) {
                throw new IllegalArgumentException(
                        "Zone conflicts with an existing hierarchy entry; refresh and try again");
            }
            throw new IllegalStateException("Unable to add smart zone", exception);
        }
    }

    private void validateParentAndChild(Connection connection, long parentId, ZoneType childType)
            throws SQLException {
        String sql = "SELECT zone_type FROM smart_zone WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, parentId);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) {
                    throw new IllegalArgumentException("Parent zone does not exist");
                }
                ZoneType parentType = ZoneType.valueOf(row.getString("zone_type"));
                if (parentType.childType() != childType) {
                    throw new IllegalArgumentException("Only City → District → Street → Building is allowed");
                }
            }
        }
    }

    private void ensureSiblingNameAvailable(Connection connection, Long parentId, String name)
            throws SQLException {
        String sql = parentId == null
                ? "SELECT 1 FROM smart_zone WHERE parent_id IS NULL AND LOWER(name) = LOWER(?)"
                : "SELECT 1 FROM smart_zone WHERE parent_id = ? AND LOWER(name) = LOWER(?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int nameParameter = 1;
            if (parentId != null) {
                statement.setLong(1, parentId);
                nameParameter = 2;
            }
            statement.setString(nameParameter, name);
            try (ResultSet row = statement.executeQuery()) {
                if (row.next()) {
                    throw new IllegalArgumentException("A zone with this name already exists under the selected parent");
                }
            }
        }
    }

    @Override
    public void recordOperation(long zoneId, ZoneOperation operation, String summary, List<Long> affectedZoneIds) {
        String updateSql = "UPDATE smart_zone SET power_on = FALSE WHERE id = ?";
        String insertSql = "INSERT INTO zone_operation (root_zone_id, operation_type, status, result_summary) " +
                "VALUES (?, ?, 'COMPLETED', ?)";
        try (Connection connection = ConnectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (operation == ZoneOperation.SHUTDOWN_POWER) {
                    try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                        for (Long affectedId : affectedZoneIds) {
                            update.setLong(1, affectedId);
                            update.addBatch();
                        }
                        update.executeBatch();
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                    insert.setLong(1, zoneId);
                    insert.setString(2, operation.name());
                    insert.setString(3, summary);
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to record smart zone operation", exception);
        }
    }

    private void rollback(Connection connection, Exception originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
