package lk.jiat.smartcity360.auth;

public record UserAccount(long id, String fullName, String username, String email,
                          String passwordHash, UserRole role, boolean active) {
    public AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(id, username, fullName, role);
    }
}
