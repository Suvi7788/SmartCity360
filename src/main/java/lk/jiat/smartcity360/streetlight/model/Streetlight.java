package lk.jiat.smartcity360.streetlight.model;

public record Streetlight(String code, Long zoneId, String location, String status,
                          LightConfiguration configuration) {
}
