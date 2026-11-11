package lk.jiat.smartcity360.auth.service;

import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.auth.PasswordHasher;
import lk.jiat.smartcity360.auth.UserAccount;
import lk.jiat.smartcity360.auth.dao.UserRepository;

public final class AuthenticationService {
    private static final String LOGIN_ERROR = "Invalid username/email or password";
    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public AuthenticationService(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    public AuthenticatedUser authenticate(String identifier, String password) {
        String cleanIdentifier = identifier == null ? "" : identifier.trim();
        if (cleanIdentifier.isEmpty() || cleanIdentifier.length() > 254 || password == null) {
            throw new IllegalArgumentException(LOGIN_ERROR);
        }
        UserAccount account = users.findByIdentifier(cleanIdentifier);
        boolean validPassword = passwordHasher.matches(password,
                account == null ? null : account.passwordHash());
        if (account == null || !account.active() || !validPassword) {
            throw new IllegalArgumentException(LOGIN_ERROR);
        }
        return account.authenticatedUser();
    }

    public AuthenticatedUser revalidate(AuthenticatedUser principal) {
        if (principal == null) {
            return null;
        }
        UserAccount account = users.findById(principal.id());
        return account == null || !account.active() ? null : account.authenticatedUser();
    }
}
