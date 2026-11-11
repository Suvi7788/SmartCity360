package lk.jiat.smartcity360.auth;

public interface PasswordHasher {
    String hash(String password);

    boolean matches(String password, String encodedPassword);
}
