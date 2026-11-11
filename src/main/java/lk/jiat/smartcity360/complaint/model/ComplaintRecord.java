package lk.jiat.smartcity360.complaint.model;

import java.time.Instant;

public record ComplaintRecord(long id, String referenceNo, String description, ComplaintSeverity severity,
                              String status, HandlerLevel currentHandler, Instant createdAt,
                              Long residentId, Long assignedUserId) {
    public ComplaintRecord(long id, String referenceNo, String description, ComplaintSeverity severity,
                           String status, HandlerLevel currentHandler, Instant createdAt) {
        this(id, referenceNo, description, severity, status, currentHandler, createdAt, null, null);
    }
}
