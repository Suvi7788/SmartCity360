package lk.jiat.smartcity360.auth.service;

import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.auth.BCryptPasswordHasher;
import lk.jiat.smartcity360.auth.UserAccount;
import lk.jiat.smartcity360.auth.UserRole;
import lk.jiat.smartcity360.auth.UserSummary;
import lk.jiat.smartcity360.auth.dao.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserManagementServiceTest {
    private static final AuthenticatedUser MANAGER =
            new AuthenticatedUser(1, "manager", "City Manager", UserRole.CITY_MANAGER);

    @Test
    void preventsOtherRolesFromManagingAccounts() {
        UserManagementService service = new UserManagementService(new TestUsers(),
                new BCryptPasswordHasher());
        AuthenticatedUser resident = new AuthenticatedUser(2, "resident", "Resident", UserRole.RESIDENT);

        assertThrows(SecurityException.class, () -> service.all(resident));
    }

    @Test
    void preventsRemovingTheLastActiveCityManager() {
        UserManagementService service = new UserManagementService(new TestUsers(),
                new BCryptPasswordHasher());

        assertThrows(IllegalArgumentException.class, () -> service.setActive(MANAGER, 2, false));
        assertThrows(IllegalArgumentException.class,
                () -> service.setRole(MANAGER, 2, UserRole.SUPERVISOR));
    }

    private static final class TestUsers implements UserRepository {
        private final UserAccount manager = new UserAccount(1, "City Manager", "manager",
                "manager@example.test", "hash", UserRole.CITY_MANAGER, true);
        private final UserAccount secondManager = new UserAccount(2, "Second Manager", "manager2",
                "manager2@example.test", "hash", UserRole.CITY_MANAGER, true);

        @Override public UserAccount findByIdentifier(String identifier) { return null; }
        @Override public UserAccount findById(long userId) {
            return userId == 1 ? manager : userId == 2 ? secondManager : null;
        }
        @Override public Long findFirstActiveIdByRole(UserRole role) { return null; }
        @Override public int countActiveByRole(UserRole role) { return role == UserRole.CITY_MANAGER ? 1 : 0; }
        @Override public List<UserSummary> findAll() { return List.of(); }
        @Override public long create(String fullName, String username, String email, String passwordHash, UserRole role) { return 0; }
        @Override public void setActive(long userId, boolean active) { }
        @Override public void setRole(long userId, UserRole role) { }
        @Override public void updatePassword(long userId, String passwordHash) { }
    }
}
