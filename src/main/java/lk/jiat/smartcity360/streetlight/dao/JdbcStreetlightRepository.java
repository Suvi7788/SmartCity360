package lk.jiat.smartcity360.streetlight.dao;

import lk.jiat.smartcity360.config.ConnectionProvider;
import lk.jiat.smartcity360.streetlight.model.LightConfiguration;
import lk.jiat.smartcity360.streetlight.model.Streetlight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class JdbcStreetlightRepository implements StreetlightRepository {
    @Override
    public void createBatch(List<Streetlight> streetlights) {
        if (streetlights == null) {
            throw new IllegalArgumentException("Streetlight batch is required");
        }
        if (streetlights.isEmpty()) {
            return;
        }
        Streetlight first = streetlights.get(0);
        LightConfiguration configuration = first == null ? null : first.configuration();
        if (configuration == null || streetlights.stream()
                .anyMatch(light -> light == null || !Objects.equals(configuration, light.configuration())
                        || light.code() == null || light.code().isBlank() || light.code().length() > 64
                        || light.location() == null || light.location().isBlank()
                        || light.location().length() > 180)) {
            throw new IllegalArgumentException("Streetlight batch contains invalid or inconsistent data");
        }
        String configSql = "INSERT INTO streetlight_config (wattage, brightness_mode, color_temperature, sensor_profile) " +
                "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)";
        String lightSql = "INSERT INTO streetlight (light_code, config_id, zone_id, location) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long configId;
                try (PreparedStatement config = connection.prepareStatement(configSql,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    config.setInt(1, configuration.wattage());
                    config.setString(2, configuration.brightnessMode());
                    config.setInt(3, configuration.colorTemperature());
                    config.setString(4, configuration.sensorProfile());
                    config.executeUpdate();
                    try (ResultSet keys = config.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("The database did not return a streetlight configuration ID");
                        }
                        configId = keys.getLong(1);
                    }
                }
                try (PreparedStatement light = connection.prepareStatement(lightSql)) {
                    for (Streetlight streetlight : streetlights) {
                        light.setString(1, streetlight.code());
                        light.setLong(2, configId);
                        if (streetlight.zoneId() == null) {
                            light.setNull(3, java.sql.Types.BIGINT);
                        } else {
                            light.setLong(3, streetlight.zoneId());
                        }
                        light.setString(4, streetlight.location());
                        light.addBatch();
                    }
                    light.executeBatch();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to create streetlights", exception);
        }
    }

    @Override
    public List<Streetlight> recent(int limit) {
        String sql = "SELECT l.light_code, l.zone_id, l.location, l.status, c.wattage, c.brightness_mode, " +
                "c.color_temperature, c.sensor_profile FROM streetlight l " +
                "JOIN streetlight_config c ON c.id = l.config_id ORDER BY l.id DESC LIMIT ?";
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(1, Math.min(limit, 100)));
            try (ResultSet rows = statement.executeQuery()) {
                List<Streetlight> lights = new ArrayList<>();
                while (rows.next()) {
                    long zoneId = rows.getLong("zone_id");
                    Long nullableZoneId = rows.wasNull() ? null : zoneId;
                    lights.add(new Streetlight(rows.getString("light_code"), nullableZoneId,
                            rows.getString("location"), rows.getString("status"),
                            new LightConfiguration(rows.getInt("wattage"), rows.getString("brightness_mode"),
                                    rows.getInt("color_temperature"), rows.getString("sensor_profile"))));
                }
                return lights;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load streetlights", exception);
        }
    }

    @Override
    public long countLights() {
        return count("streetlight");
    }

    @Override
    public long countConfigurations() {
        return count("streetlight_config");
    }

    private long count(String table) {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (Connection connection = ConnectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getLong(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to count " + table + " records", exception);
        }
    }
}
