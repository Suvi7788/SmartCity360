package lk.jiat.smartcity360.auth;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class PasswordHashTool {
    private PasswordHashTool() {
    }

    public static void main(String[] args) {
        Console console = System.console();
        char[] password = readPassword(console);
        if (password == null) {
            throw new IllegalStateException("Password input was cancelled");
        }
        try {
            String value = new String(password);
            BCryptPasswordHasher.validatePassword(value);
            String hash = new BCryptPasswordHasher().hash(value);
            if (console == null) {
                System.out.println(hash);
            } else {
                console.printf("%s%n", hash);
            }
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static char[] readPassword(Console console) {
        if (console != null) {
            return console.readPassword("Initial City Manager password: ");
        }

        System.err.print("Initial City Manager password: ");
        System.err.flush();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            String password = reader.readLine();
            return password == null ? null : password.toCharArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read the password from the terminal", exception);
        }
    }
}
