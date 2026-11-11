package lk.jiat.smartcity360.securitystream.dao;

import lk.jiat.smartcity360.config.ConnectionProvider;
import lk.jiat.smartcity360.securitystream.model.StreamRun;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class JdbcStreamRunRepository implements StreamRunRepository {
    @Override
    public long save(String meterId, String decoratorChain, String originalPayload, String processedPayload) {
        String sql = "INSERT INTO security_stream_run " +
                "(meter_id, decorator_chain, original_payload, processed_payload) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, meterId);
            statement.setString(2, decoratorChain);
            statement.setString(3, originalPayload);
            statement.setString(4, processedPayload);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("The database did not return a stream run ID");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save the security stream run", exception);
        }
    }

    @Override
    public List<StreamRun> findRecent(int limit) {
        String sql = "SELECT id, meter_id, decorator_chain, original_payload, processed_payload, created_at " +
                "FROM security_stream_run ORDER BY created_at DESC LIMIT ?";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(1, Math.min(limit, 100)));
            try (ResultSet rows = statement.executeQuery()) {
                List<StreamRun> runs = new ArrayList<>();
                while (rows.next()) {
                    runs.add(new StreamRun(rows.getLong("id"), rows.getString("meter_id"),
                            rows.getString("decorator_chain"), rows.getString("original_payload"),
                            rows.getString("processed_payload"), rows.getTimestamp("created_at").toInstant()));
                }
                return runs;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load security stream runs", exception);
        }
    }
}
