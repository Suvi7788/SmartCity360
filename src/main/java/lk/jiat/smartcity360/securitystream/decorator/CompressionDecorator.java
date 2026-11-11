package lk.jiat.smartcity360.securitystream.decorator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public final class CompressionDecorator extends StreamDecorator {
    public CompressionDecorator(StreamProcessor delegate) {
        super(delegate);
    }

    @Override
    public byte[] process(byte[] input) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
                gzip.write(processDelegate(input));
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to compress meter data", exception);
        }
    }
}
