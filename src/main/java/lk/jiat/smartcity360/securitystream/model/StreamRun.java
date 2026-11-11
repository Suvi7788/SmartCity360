package lk.jiat.smartcity360.securitystream.model;

import java.time.Instant;

public record StreamRun(long id, String meterId, String decoratorChain, String originalPayload,
                        String processedPayload, Instant createdAt) {
}
