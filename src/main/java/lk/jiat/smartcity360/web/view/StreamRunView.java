package lk.jiat.smartcity360.web.view;

import lk.jiat.smartcity360.securitystream.model.StreamRun;

import java.time.Instant;

public final class StreamRunView {
    private final long id;
    private final String meterId;
    private final String decoratorChain;
    private final String originalPayload;
    private final String processedPayload;
    private final Instant createdAt;

    public StreamRunView(StreamRun run) {
        this.id = run.id();
        this.meterId = run.meterId();
        this.decoratorChain = run.decoratorChain();
        this.originalPayload = run.originalPayload();
        this.processedPayload = run.processedPayload();
        this.createdAt = run.createdAt();
    }

    public long getId() {
        return id;
    }

    public String getMeterId() {
        return meterId;
    }

    public String getDecoratorChain() {
        return decoratorChain;
    }

    public String getOriginalPayload() {
        return originalPayload;
    }

    public String getProcessedPayload() {
        return processedPayload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
