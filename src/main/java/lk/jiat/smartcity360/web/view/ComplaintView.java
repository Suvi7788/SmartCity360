package lk.jiat.smartcity360.web.view;

import lk.jiat.smartcity360.complaint.model.ComplaintRecord;

import java.time.Instant;

public final class ComplaintView {
    private final long id;
    private final String referenceNo;
    private final String description;
    private final String severity;
    private final String status;
    private final String currentHandler;
    private final Instant createdAt;

    public ComplaintView(ComplaintRecord complaint) {
        this.id = complaint.id();
        this.referenceNo = complaint.referenceNo();
        this.description = complaint.description();
        this.severity = complaint.severity().name();
        this.status = complaint.status();
        this.currentHandler = complaint.currentHandler().name();
        this.createdAt = complaint.createdAt();
    }

    public long getId() {
        return id;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }

    public String getStatus() {
        return status;
    }

    public String getCurrentHandler() {
        return currentHandler;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
