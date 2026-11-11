package lk.jiat.smartcity360.complaint.model;

import lk.jiat.smartcity360.auth.UserRole;

import java.time.Instant;

public record EscalationRecord(HandlerLevel handler, String action, String note, Instant occurredAt,
                               Long actorId, UserRole actorRole) {
    public EscalationRecord(HandlerLevel handler, String action, String note, Instant occurredAt) {
        this(handler, action, note, occurredAt, null, null);
    }
}
