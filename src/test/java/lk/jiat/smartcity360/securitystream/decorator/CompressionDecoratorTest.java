package lk.jiat.smartcity360.securitystream.decorator;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CompressionDecoratorTest {
    @Test
    void compressesTheDelegatedMeterPayload() throws Exception {
        byte[] source = "{\"energyKwh\":42.8}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] compressed = new CompressionDecorator(new RawMeterStream()).process(source);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            gzip.transferTo(output);
        }

        assertArrayEquals(source, output.toByteArray());
    }
}
