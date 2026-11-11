package lk.jiat.smartcity360.web.view;

import lk.jiat.smartcity360.streetlight.model.Streetlight;

public final class StreetlightView {
    private final String code;
    private final Long zoneId;
    private final String location;
    private final String status;
    private final LightConfigurationView configuration;

    public StreetlightView(Streetlight streetlight) {
        this.code = streetlight.code();
        this.zoneId = streetlight.zoneId();
        this.location = streetlight.location();
        this.status = streetlight.status();
        this.configuration = new LightConfigurationView(streetlight.configuration());
    }

    public String getCode() {
        return code;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    public LightConfigurationView getConfiguration() {
        return configuration;
    }
}
