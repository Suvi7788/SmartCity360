package lk.jiat.smartcity360.securitystream.decorator;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public final class EncryptionDecorator extends StreamDecorator {
    private static final int IV_LENGTH = 12;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionDecorator(StreamProcessor delegate) {
        super(delegate);
    }

    @Override
    public byte[] process(byte[] input) {
        try {
            byte[] key = Base64.getDecoder().decode(requiredKey("SMARTCITY_STREAM_AES_KEY"));
            if (key.length != 16 && key.length != 24 && key.length != 32) {
                throw new IllegalStateException("SMARTCITY_STREAM_AES_KEY must decode to 16, 24, or 32 bytes");
            }
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            byte[] ciphertext = cipher.doFinal(processDelegate(input));
            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
            return result;
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("Unable to encrypt meter data; verify the AES key configuration", exception);
        }
    }

    static String requiredKey(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required when the encryption decorator is enabled");
        }
        return value;
    }
}
