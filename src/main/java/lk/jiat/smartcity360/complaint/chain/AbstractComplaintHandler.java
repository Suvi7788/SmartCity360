package lk.jiat.smartcity360.complaint.chain;

import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;

abstract class AbstractComplaintHandler implements ComplaintHandler {
    private final HandlerLevel level;
    private final String responsibility;
    private ComplaintHandler next;

    AbstractComplaintHandler(HandlerLevel level, String responsibility) {
        this.level = level;
        this.responsibility = responsibility;
    }

    void setNext(ComplaintHandler next) {
        this.next = next;
    }

    @Override
    public HandlerLevel level() {
        return level;
    }

    @Override
    public String responsibility() {
        return responsibility;
    }

    @Override
    public HandlerLevel handlerFor(ComplaintSeverity severity) {
        if (accepts(severity)) {
            return level;
        }
        if (next == null) {
            throw new IllegalStateException("No complaint handler accepts severity " + severity);
        }
        return next.handlerFor(severity);
    }

    @Override
    public ComplaintEscalation escalate() {
        if (next != null) {
            return new ComplaintEscalation(level, next.level(), "ESCALATED",
                    "Escalated from " + level + " to " + next.level() + ".", false);
        }
        return new ComplaintEscalation(level, level, "FINAL_HANDLING",
                responsibility + " Final review completed; the complaint remains open until resolved.", true);
    }

    @Override
    public ComplaintEscalation escalationFrom(HandlerLevel current) {
        if (level == current) {
            return escalate();
        }
        if (next == null) {
            return new ComplaintEscalation(current, null, "UNHANDLED",
                    "No configured complaint handler accepts escalation from " + current + ".", false);
        }
        return next.escalationFrom(current);
    }

    protected abstract boolean accepts(ComplaintSeverity severity);
}
