package lk.jiat.smartcity360.securitystream.service;

import lk.jiat.smartcity360.securitystream.dao.StreamRunRepository;
import lk.jiat.smartcity360.securitystream.model.StreamRun;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityStreamServiceTest {
    @Test
    void dynamicallyAppliesSelectedDecoratorAndPersistsBothRepresentations() throws Exception {
        CapturingRepository repository = new CapturingRepository();
        SecurityStreamService service = new SecurityStreamService(repository);

        service.process("MTR-42", 18.5, List.of("Compression"));

        assertEquals("MTR-42", repository.meterId);
        assertEquals("Compression", repository.chain);
        assertTrue(repository.original.contains("\"energyKwh\":18.5"));
        byte[] compressed = Base64.getDecoder().decode(repository.processed);
        ByteArrayOutputStream decoded = new ByteArrayOutputStream();
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            gzip.transferTo(decoded);
        }
        assertEquals(repository.original, decoded.toString(StandardCharsets.UTF_8));
    }

    private static final class CapturingRepository implements StreamRunRepository {
        private String meterId;
        private String chain;
        private String original;
        private String processed;

        @Override
        public long save(String meterId, String decoratorChain, String originalPayload, String processedPayload) {
            this.meterId = meterId;
            this.chain = decoratorChain;
            this.original = originalPayload;
            this.processed = processedPayload;
            return 1;
        }

        @Override
        public List<StreamRun> findRecent(int limit) {
            return List.of(new StreamRun(1, meterId, chain, original, processed, Instant.now()));
        }
    }
}
