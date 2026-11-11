package lk.jiat.smartcity360.securitystream.decorator;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationDecoratorTest {
    private static final byte[] KEY = new byte[32];
    private static final byte[] OTHER_KEY = new byte[32];

    static {
        Arrays.fill(KEY, (byte) 7);
        Arrays.fill(OTHER_KEY, (byte) 9);
    }

    @Test
    void verifiesValidSignatureAndReturnsOriginalPayload() {
        byte[] payload = "meter reading".getBytes(StandardCharsets.UTF_8);
        AuthenticationDecorator signer = new AuthenticationDecorator(new RawMeterStream(), KEY);
        byte[] signed = signer.process(payload);

        assertArrayEquals(payload, signer.verify(signed));
    }

    @Test
    void rejectsModifiedPayload() {
        AuthenticationDecorator decorator = new AuthenticationDecorator(new RawMeterStream(), KEY);
        byte[] signed = decorator.process("meter reading".getBytes(StandardCharsets.UTF_8));
        signed[0] ^= 1;

        assertThrows(SecurityException.class, () -> decorator.verify(signed));
    }

    @Test
    void rejectsAnInvalidSignature() {
        AuthenticationDecorator decorator = new AuthenticationDecorator(new RawMeterStream(), KEY);
        byte[] signed = decorator.process("meter reading".getBytes(StandardCharsets.UTF_8));
        signed[signed.length - 1] ^= 1;

        assertThrows(SecurityException.class, () -> decorator.verify(signed));
    }

    @Test
    void rejectsSignatureWhenVerificationKeyIsIncorrect() {
        AuthenticationDecorator signer = new AuthenticationDecorator(new RawMeterStream(), KEY);
        AuthenticationDecorator verifier = new AuthenticationDecorator(new RawMeterStream(), OTHER_KEY);
        byte[] signed = signer.process("meter reading".getBytes(StandardCharsets.UTF_8));

        assertThrows(SecurityException.class, () -> verifier.verify(signed));
    }
}
