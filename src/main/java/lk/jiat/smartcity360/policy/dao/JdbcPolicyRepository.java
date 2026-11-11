package lk.jiat.smartcity360.policy.dao;

import lk.jiat.smartcity360.config.ConnectionProvider;
import lk.jiat.smartcity360.policy.model.CityPolicy;
import lk.jiat.smartcity360.policy.model.PolicyEvaluation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JdbcPolicyRepository implements PolicyRepository {
    @Override
    public List<CityPolicy> findAll() {
        String sql = "SELECT id, name, expression_text, active FROM city_policy ORDER BY updated_at DESC";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<CityPolicy> policies = new ArrayList<>();
            while (rows.next()) {
                policies.add(new CityPolicy(rows.getLong("id"), rows.getString("name"),
                        rows.getString("expression_text"), rows.getBoolean("active")));
            }
            return policies;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load city policies", exception);
        }
    }

    @Override
    public CityPolicy find(long id) {
        String sql = "SELECT id, name, expression_text, active FROM city_policy WHERE id = ?";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) {
                    throw new IllegalArgumentException("Policy does not exist");
                }
                return new CityPolicy(row.getLong("id"), row.getString("name"),
                        row.getString("expression_text"), row.getBoolean("active"));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load city policy", exception);
        }
    }

    @Override
    public long save(Long id, String name, String expression, boolean active) {
        if (id == null) {
            String sql = "INSERT INTO city_policy (name, expression_text, active) VALUES (?, ?, ?)";
            try (Connection connection = ConnectionProvider.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, name);
                statement.setString(2, expression);
                statement.setBoolean(3, active);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                    throw new SQLException("The database did not return a policy ID");
                }
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to create city policy", exception);
            }
        }
        String sql = "UPDATE city_policy SET name = ?, expression_text = ?, active = ? WHERE id = ?";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, expression);
            statement.setBoolean(3, active);
            statement.setLong(4, id);
            if (statement.executeUpdate() == 0) {
                try (PreparedStatement exists = connection.prepareStatement("SELECT 1 FROM city_policy WHERE id = ?")) {
                    exists.setLong(1, id);
                    try (ResultSet row = exists.executeQuery()) {
                        if (!row.next()) {
                            throw new IllegalArgumentException("Policy does not exist");
                        }
                    }
                }
            }
            return id;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update city policy", exception);
        }
    }

    @Override
    public void saveEvaluation(Long policyId, Map<String, Double> values, Boolean result, String error) {
        String sql = "INSERT INTO policy_evaluation (policy_id, input_values, result, validation_error) " +
                "VALUES (?, ?, ?, ?)";
        String json = values.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
                .collect(Collectors.joining(",", "{", "}"));
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (policyId == null) {
                statement.setNull(1, java.sql.Types.BIGINT);
            } else {
                statement.setLong(1, policyId);
            }
            statement.setString(2, json);
            if (result == null) {
                statement.setNull(3, java.sql.Types.BOOLEAN);
            } else {
                statement.setBoolean(3, result);
            }
            statement.setString(4, error);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save policy evaluation", exception);
        }
    }

    @Override
    public List<PolicyEvaluation> recentEvaluations() {
        String sql = "SELECT e.id, e.policy_id, p.name, e.input_values, e.result, e.validation_error, e.evaluated_at " +
                "FROM policy_evaluation e LEFT JOIN city_policy p ON p.id = e.policy_id " +
                "ORDER BY e.evaluated_at DESC LIMIT 20";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<PolicyEvaluation> evaluations = new ArrayList<>();
            while (rows.next()) {
                long policyId = rows.getLong("policy_id");
                Long nullablePolicyId = rows.wasNull() ? null : policyId;
                boolean result = rows.getBoolean("result");
                Boolean nullableResult = rows.wasNull() ? null : result;
                evaluations.add(new PolicyEvaluation(rows.getLong("id"), nullablePolicyId, rows.getString("name"),
                        rows.getString("input_values"), nullableResult, rows.getString("validation_error"),
                        rows.getTimestamp("evaluated_at").toInstant()));
            }
            return evaluations;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load policy evaluations", exception);
        }
    }
}
