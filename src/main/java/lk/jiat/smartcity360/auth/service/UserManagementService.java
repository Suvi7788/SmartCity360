package lk.jiat.smartcity360.auth.service;

import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.auth.BCryptPasswordHasher;
import lk.jiat.smartcity360.auth.PasswordHasher;
import lk.jiat.smartcity360.auth.UserAccount;
import lk.jiat.smartcity360.auth.UserRole;
import lk.jiat.smartcity360.auth.UserSummary;
import lk.jiat.smartcity360.auth.dao.UserRepository;

import java.util.List;
import java.util.regex.Pattern;

public final class UserManagementService {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,64}");
    private static final Pattern EMAIL = Pattern.compile("[^\\s@]{1,64}@[^\\s@.]+(?:\\.[^\\s@.]+)+");

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public UserManagementService(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    public long create(AuthenticatedUser actor, String fullName, String username, String email,
                       String password, UserRole role) {
        requireCityManager(actor);
        String cleanName = fullName == null ? "" : fullName.trim();
        String cleanUsername = username == null ? "" : username.trim();
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        if (cleanName.isEmpty() || cleanName.length() > 120) {
            throw new IllegalArgumentException("Full name must contain 1-120 characters");
        }
        if (!USERNAME.matcher(cleanUsername).matches()) {
            throw new IllegalArgumentException("Username must be 3-64 letters, numbers, dots, underscores, or hyphens");
        }
        if (cleanEmail.length() > 254 || !EMAIL.matcher(cleanEmail).matches()) {
            throw new IllegalArgumentException("Enter a valid email address");
        }
        if (role == null) {
            throw new IllegalArgumentException("Select one of the four supported roles");
        }
        BCryptPasswordHasher.validatePassword(password);
        return users.create(cleanName, cleanUsername, cleanEmail, passwordHasher.hash(password), role);
    }

    public List<UserSummary> all(AuthenticatedUser actor) {
        requireCityManager(actor);
        return users.findAll();
    }

    public void setActive(AuthenticatedUser actor, long userId, boolean active) {
        requireCityManager(actor);
        requirePositiveId(userId);
        if (actor.id() == userId && !active) {
            throw new IllegalArgumentException("You cannot deactivate your own account");
        }
        UserAccount target = requireExistingUser(userId);
        if (!active && target.active() && target.role() == UserRole.CITY_MANAGER
                && users.countActiveByRole(UserRole.CITY_MANAGER) <= 1) {
            throw new IllegalArgumentException("At least one active City Manager account must remain");
        }
        users.setActive(userId, active);
    }

    public void setRole(AuthenticatedUser actor, long userId, UserRole role) {
        requireCityManager(actor);
        requirePositiveId(userId);
        if (role == null) {
            throw new IllegalArgumentException("Select one of the four supported roles");
        }
        if (actor.id() == userId && role != UserRole.CITY_MANAGER) {
            throw new IllegalArgumentException("You cannot remove your own City Manager access");
        }
        UserAccount target = requireExistingUser(userId);
        if (target.active() && target.role() == UserRole.CITY_MANAGER && role != UserRole.CITY_MANAGER
                && users.countActiveByRole(UserRole.CITY_MANAGER) <= 1) {
            throw new IllegalArgumentException("At least one active City Manager account must remain");
        }
        users.setRole(userId, role);
    }

    public void resetPassword(AuthenticatedUser actor, long userId, String password) {
        requireCityManager(actor);
        requirePositiveId(userId);
        BCryptPasswordHasher.validatePassword(password);
        users.updatePassword(userId, passwordHasher.hash(password));
    }

    private void requireCityManager(AuthenticatedUser actor) {
        if (actor == null || actor.role() != UserRole.CITY_MANAGER) {
            throw new SecurityException("City Manager access is required");
        }
    }

    private void requirePositiveId(long id) {
        if (id < 1) {
            throw new IllegalArgumentException("User ID must be positive");
        }
    }

    private UserAccount requireExistingUser(long userId) {
        UserAccount account = users.findById(userId);
        if (account == null) {
            throw new IllegalArgumentException("User does not exist");
        }
        return account;
    }
}
