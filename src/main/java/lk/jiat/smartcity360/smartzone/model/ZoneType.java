package lk.jiat.smartcity360.smartzone.model;

public enum ZoneType {
    CITY, DISTRICT, STREET, BUILDING;

    public ZoneType childType() {
        return switch (this) {
            case CITY -> DISTRICT;
            case DISTRICT -> STREET;
            case STREET -> BUILDING;
            case BUILDING -> null;
        };
    }

    public static ZoneType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invalid zone type");
        }
        try {
            return ZoneType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid zone type", exception);
        }
    }
}
