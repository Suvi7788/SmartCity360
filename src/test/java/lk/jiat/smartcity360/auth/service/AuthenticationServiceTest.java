package lk.jiat.smartcity360.auth.service;

import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.auth.BCryptPasswordHasher;
import lk.jiat.smartcity360.auth.UserAccount;
import lk.jiat.smartcity360.auth.UserRole;
import lk.jiat.smartcity360.auth.UserSummary;
import lk.jiat.smartcity360.auth.dao.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationServiceTest {
    @Test
    void authenticatesOnlyAnActiveAccountWithTheMatchingPassword() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        String hash = hasher.hash("UniversityDemo!2025");
        UserAccount account = new UserAccount(1, "City Manager", "manager", "manager@example.test",
                hash, UserRole.CITY_MANAGER, true);
        AuthenticationService service = new AuthenticationService(new TestUsers(account), hasher);

        AuthenticatedUser result = service.authenticate("manager", "UniversityDemo!2025");

        assertEquals(1, result.id());
        assertEquals(UserRole.CITY_MANAGER, result.role());
        assertThrows(IllegalArgumentException.class,
                () -> service.authenticate("manager", "incorrect-password"));
    }

    @Test
    void usesTheSameFailureForUnknownAndInactiveAccounts() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        UserAccount inactive = new UserAccount(2, "Inactive User", "inactive", "inactive@example.test",
                hasher.hash("UniversityDemo!2025"), UserRole.RESIDENT, false);
        AuthenticationService service = new AuthenticationService(new TestUsers(inactive), hasher);

        IllegalArgumentException inactiveFailure = assertThrows(IllegalArgumentException.class,
                () -> service.authenticate("inactive", "UniversityDemo!2025"));
        IllegalArgumentException unknownFailure = assertThrows(IllegalArgumentException.class,
                () -> service.authenticate("missing", "UniversityDemo!2025"));
        assertEquals(inactiveFailure.getMessage(), unknownFailure.getMessage());
    }

    @Test
    void revalidatesStoredAccountStatusAndCurrentRole() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        UserAccount active = new UserAccount(1, "User", "user", "user@example.test",
                hasher.hash("UniversityDemo!2025"), UserRole.CITY_MANAGER, true);
        AuthenticationService service = new AuthenticationService(new TestUsers(active), hasher);
        AuthenticatedUser cachedPrincipal = new AuthenticatedUser(1, "user", "User", UserRole.RESIDENT);

        assertEquals(UserRole.CITY_MANAGER, service.revalidate(cachedPrincipal).role());

        UserAccount inactive = new UserAccount(1, "User", "user", "user@example.test",
                active.passwordHash(), UserRole.CITY_MANAGER, false);
        assertNull(new AuthenticationService(new TestUsers(inactive), hasher).revalidate(cachedPrincipal));
        assertNull(service.revalidate(null));
    }

    private static final class TestUsers implements UserRepository {
        private final UserAccount account;

        private TestUsers(UserAccount account) {
            this.account = account;
        }

        @Override public UserAccount findByIdentifier(String identifier) {
            return account != null && (account.username().equals(identifier) || account.email().equals(identifier))
                    ? account : null;
        }
        @Override public UserAccount findById(long userId) { return account != null && account.id() == userId ? account : null; }
        @Override public Long findFirstActiveIdByRole(UserRole role) { return null; }
        @Override public int countActiveByRole(UserRole role) { return account != null && account.active() && account.role() == role ? 1 : 0; }
        @Override public List<UserSummary> findAll() { return List.of(); }
        @Override public long create(String fullName, String username, String email, String passwordHash, UserRole role) { return 0; }
        @Override public void setActive(long userId, boolean active) { }
        @Override public void setRole(long userId, UserRole role) { }
        @Override public void updatePassword(long userId, String passwordHash) { }
    }
}
