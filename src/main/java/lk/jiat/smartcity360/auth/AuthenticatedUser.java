package lk.jiat.smartcity360.auth;

import java.io.Serializable;

public record AuthenticatedUser(long id, String username, String fullName, UserRole role)
        implements Serializable {
    public AuthenticatedUser {
        if (id < 1 || username == null || fullName == null || role == null) {
            throw new IllegalArgumentException("Authenticated user identity is invalid");
        }
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public String getRoleName() {
        return role.name();
    }

    public String getInitials() {
        return fullName.isBlank() ? "SC" : fullName.substring(0, 1).toUpperCase();
    }
}
