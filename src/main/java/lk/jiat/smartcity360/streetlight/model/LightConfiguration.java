package lk.jiat.smartcity360.streetlight.model;

import java.util.Set;

public record LightConfiguration(int wattage, String brightnessMode, int colorTemperature, String sensorProfile) {
    private static final Set<String> ALLOWED_BRIGHTNESS_MODES =
            Set.of("Adaptive", "Constant", "Motion-triggered");
    private static final Set<String> ALLOWED_SENSOR_PROFILES =
            Set.of("Standard", "Traffic-aware", "Pedestrian-aware");

    public LightConfiguration {
        if (wattage < 1 || wattage > 1000 || colorTemperature < 1000 || colorTemperature > 10000) {
            throw new IllegalArgumentException("Invalid streetlight configuration values");
        }
        brightnessMode = brightnessMode == null ? "" : brightnessMode.trim();
        sensorProfile = sensorProfile == null ? "" : sensorProfile.trim();
        if (brightnessMode.length() > 32 || !ALLOWED_BRIGHTNESS_MODES.contains(brightnessMode)) {
            throw new IllegalArgumentException("Select a supported brightness mode");
        }
        if (sensorProfile.length() > 48 || !ALLOWED_SENSOR_PROFILES.contains(sensorProfile)) {
            throw new IllegalArgumentException("Select a supported sensor profile");
        }
    }
}
