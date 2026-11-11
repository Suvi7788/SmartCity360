package lk.jiat.smartcity360.auth;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class BCryptPasswordHasher implements PasswordHasher {
    private static final int WORK_FACTOR = 12;
    private static final String DUMMY_HASH =
            BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt(WORK_FACTOR));

    @Override
    public String hash(String password) {
        validatePassword(password);
        return BCrypt.hashpw(password, BCrypt.gensalt(WORK_FACTOR));
    }

    @Override
    public boolean matches(String password, String encodedPassword) {
        if (password == null || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            BCrypt.checkpw("invalid-password-input", DUMMY_HASH);
            return false;
        }
        try {
            return BCrypt.checkpw(password, encodedPassword == null ? DUMMY_HASH : encodedPassword);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must contain 12-72 UTF-8 bytes");
        }
    }
}
