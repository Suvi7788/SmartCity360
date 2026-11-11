package lk.jiat.smartcity360.auth.dao;

import lk.jiat.smartcity360.auth.UserAccount;
import lk.jiat.smartcity360.auth.UserRole;
import lk.jiat.smartcity360.auth.UserSummary;
import lk.jiat.smartcity360.config.ConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class JdbcUserRepository implements UserRepository {
    @Override
    public UserAccount findByIdentifier(String identifier) {
        String sql = "SELECT id, full_name, username, email, password_hash, role, active " +
                "FROM app_user WHERE username = ? OR email = ? LIMIT 1";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, identifier);
            statement.setString(2, identifier);
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? readAccount(row) : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to authenticate user", exception);
        }
    }

    @Override
    public UserAccount findById(long userId) {
        String sql = "SELECT id, full_name, username, email, password_hash, role, active " +
                "FROM app_user WHERE id = ?";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? readAccount(row) : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to validate user session", exception);
        }
    }

    @Override
    public Long findFirstActiveIdByRole(UserRole role) {
        String sql = "SELECT id FROM app_user WHERE role = ? AND active = TRUE ORDER BY id LIMIT 1";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.name());
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? row.getLong("id") : null;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to find an active complaint handler", exception);
        }
    }

    @Override
    public int countActiveByRole(UserRole role) {
        String sql = "SELECT COUNT(*) FROM app_user WHERE role = ? AND active = TRUE";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.name());
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                return row.getInt(1);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to validate active City Manager accounts", exception);
        }
    }

    @Override
    public List<UserSummary> findAll() {
        String sql = "SELECT id, full_name, username, email, role, active " +
                "FROM app_user ORDER BY full_name, id";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<UserSummary> users = new ArrayList<>();
            while (rows.next()) {
                users.add(new UserSummary(rows.getLong("id"), rows.getString("full_name"),
                        rows.getString("username"), rows.getString("email"),
                        UserRole.valueOf(rows.getString("role")), rows.getBoolean("active")));
            }
            return List.copyOf(users);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load users", exception);
        }
    }

    @Override
    public long create(String fullName, String username, String email, String passwordHash, UserRole role) {
        String sql = "INSERT INTO app_user (full_name, username, email, password_hash, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, fullName);
            statement.setString(2, username);
            statement.setString(3, email);
            statement.setString(4, passwordHash);
            statement.setString(5, role.name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("The database did not return a user ID");
                }
                return keys.getLong(1);
            }
        } catch (SQLException exception) {
            if (exception.getSQLState() != null && exception.getSQLState().startsWith("23")) {
                throw new IllegalArgumentException("Username or email is already registered");
            }
            throw new IllegalStateException("Unable to create user", exception);
        }
    }

    @Override
    public void setActive(long userId, boolean active) {
        updateOne("UPDATE app_user SET active = ? WHERE id = ?", statement -> {
            statement.setBoolean(1, active);
            statement.setLong(2, userId);
        });
    }

    @Override
    public void setRole(long userId, UserRole role) {
        updateOne("UPDATE app_user SET role = ? WHERE id = ?", statement -> {
            statement.setString(1, role.name());
            statement.setLong(2, userId);
        });
    }

    @Override
    public void updatePassword(long userId, String passwordHash) {
        updateOne("UPDATE app_user SET password_hash = ? WHERE id = ?", statement -> {
            statement.setString(1, passwordHash);
            statement.setLong(2, userId);
        });
    }

    private void updateOne(String sql, StatementBinder binder) {
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("User does not exist");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update user", exception);
        }
    }

    private UserAccount readAccount(ResultSet row) throws SQLException {
        return new UserAccount(row.getLong("id"), row.getString("full_name"),
                row.getString("username"), row.getString("email"), row.getString("password_hash"),
                UserRole.valueOf(row.getString("role")), row.getBoolean("active"));
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
