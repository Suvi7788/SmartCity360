package lk.jiat.smartcity360.auth;

import lk.jiat.smartcity360.complaint.model.HandlerLevel;

public enum UserRole {
    RESIDENT,
    LOCAL_STAFF,
    SUPERVISOR,
    CITY_MANAGER;

    public HandlerLevel complaintLevel() {
        return switch (this) {
            case LOCAL_STAFF -> HandlerLevel.LOCAL_STAFF;
            case SUPERVISOR -> HandlerLevel.SUPERVISOR;
            case CITY_MANAGER -> HandlerLevel.CITY_MANAGER;
            case RESIDENT -> null;
        };
    }
}
