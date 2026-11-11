package lk.jiat.smartcity360.web.view;

import lk.jiat.smartcity360.complaint.model.EscalationRecord;

import java.time.Instant;

public final class EscalationView {
    private final String handler;
    private final String action;
    private final String note;
    private final Instant occurredAt;
    private final String actorRole;

    public EscalationView(EscalationRecord event) {
        this.handler = event.handler().name();
        this.action = event.action();
        this.note = event.note();
        this.occurredAt = event.occurredAt();
        this.actorRole = event.actorRole() == null ? "LEGACY" : event.actorRole().name();
    }

    public String getHandler() {
        return handler;
    }

    public String getAction() {
        return action;
    }

    public String getNote() {
        return note;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getActorRole() {
        return actorRole;
    }
}
