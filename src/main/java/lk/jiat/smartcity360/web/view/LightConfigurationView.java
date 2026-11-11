package lk.jiat.smartcity360.web.view;

import lk.jiat.smartcity360.streetlight.model.LightConfiguration;

public final class LightConfigurationView {
    private final int wattage;
    private final String brightnessMode;
    private final int colorTemperature;
    private final String sensorProfile;

    public LightConfigurationView(LightConfiguration configuration) {
        this.wattage = configuration.wattage();
        this.brightnessMode = configuration.brightnessMode();
        this.colorTemperature = configuration.colorTemperature();
        this.sensorProfile = configuration.sensorProfile();
    }

    public int getWattage() {
        return wattage;
    }

    public String getBrightnessMode() {
        return brightnessMode;
    }

    public int getColorTemperature() {
        return colorTemperature;
    }

    public String getSensorProfile() {
        return sensorProfile;
    }
}
