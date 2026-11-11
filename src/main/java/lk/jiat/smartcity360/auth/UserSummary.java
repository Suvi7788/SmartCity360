package lk.jiat.smartcity360.auth;

public record UserSummary(long id, String fullName, String username, String email,
                          UserRole role, boolean active) {
    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public String getRoleName() { return role.name(); }
    public boolean isActive() { return active; }
}
