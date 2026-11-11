package lk.jiat.smartcity360.dashboard.dao;

import lk.jiat.smartcity360.config.ConnectionProvider;
import lk.jiat.smartcity360.dashboard.model.DashboardStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DashboardRepository {
    public DashboardStats loadStats() {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM security_stream_run) AS stream_runs, " +
                "(SELECT COUNT(*) FROM smart_zone) AS zones, " +
                "(SELECT COUNT(*) FROM complaint WHERE status <> 'RESOLVED') AS open_complaints, " +
                "(SELECT COUNT(*) FROM streetlight) AS streetlights, " +
                "(SELECT COUNT(*) FROM city_policy WHERE active = TRUE) AS active_policies";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet row = statement.executeQuery()) {
            if (!row.next()) {
                throw new SQLException("Dashboard statistics query returned no result");
            }
            return new DashboardStats(row.getLong("stream_runs"), row.getLong("zones"),
                    row.getLong("open_complaints"), row.getLong("streetlights"),
                    row.getLong("active_policies"));
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load dashboard statistics", exception);
        }
    }
}
