package lk.jiat.smartcity360.securitystream.decorator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.security.MessageDigest;

public final class AuthenticationDecorator extends StreamDecorator {
    private static final String ALGORITHM = "HmacSHA256";
    private static final int SIGNATURE_LENGTH = 32;
    private final byte[] key;

    public AuthenticationDecorator(StreamProcessor delegate) {
        this(delegate, decodeKey(EncryptionDecorator.requiredKey("SMARTCITY_STREAM_HMAC_KEY")));
    }

    public AuthenticationDecorator(StreamProcessor delegate, byte[] key) {
        super(delegate);
        this.key = validateKey(key);
    }

    @Override
    public byte[] process(byte[] input) {
        return appendSignature(processDelegate(input), key);
    }

    public byte[] verify(byte[] signedPayload) {
        if (signedPayload == null || signedPayload.length < SIGNATURE_LENGTH) {
            throw new SecurityException("Authenticated payload is missing its HMAC-SHA256 signature");
        }
        int payloadLength = signedPayload.length - SIGNATURE_LENGTH;
        byte[] payload = Arrays.copyOf(signedPayload, payloadLength);
        byte[] suppliedSignature = Arrays.copyOfRange(signedPayload, payloadLength, signedPayload.length);
        byte[] expectedSignature = sign(payload, key);
        if (!MessageDigest.isEqual(expectedSignature, suppliedSignature)) {
            throw new SecurityException("Authenticated payload signature is invalid or the payload was modified");
        }
        return payload;
    }

    private static byte[] appendSignature(byte[] payload, byte[] key) {
        byte[] signature = sign(payload, key);
        byte[] result = Arrays.copyOf(payload, payload.length + signature.length);
        System.arraycopy(signature, 0, result, payload.length, signature.length);
        return result;
    }

    private static byte[] sign(byte[] payload, byte[] key) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key, ALGORITHM));
            return mac.doFinal(payload);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to sign meter data using HMAC-SHA256", exception);
        }
    }

    private static byte[] decodeKey(String encoded) {
        try {
            return validateKey(Base64.getDecoder().decode(encoded));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("SMARTCITY_STREAM_HMAC_KEY must be valid Base64 encoding", exception);
        }
    }

    private static byte[] validateKey(byte[] key) {
        if (key == null || key.length < SIGNATURE_LENGTH) {
            throw new IllegalArgumentException("HMAC-SHA256 key must contain at least 32 bytes");
        }
        return key.clone();
    }
}
