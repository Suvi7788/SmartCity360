package lk.jiat.smartcity360.auth.dao;

import lk.jiat.smartcity360.auth.UserAccount;
import lk.jiat.smartcity360.auth.UserRole;
import lk.jiat.smartcity360.auth.UserSummary;

import java.util.List;

public interface UserRepository {
    UserAccount findByIdentifier(String identifier);

    UserAccount findById(long userId);

    Long findFirstActiveIdByRole(UserRole role);

    int countActiveByRole(UserRole role);

    List<UserSummary> findAll();

    long create(String fullName, String username, String email, String passwordHash, UserRole role);

    void setActive(long userId, boolean active);

    void setRole(long userId, UserRole role);

    void updatePassword(long userId, String passwordHash);
}
