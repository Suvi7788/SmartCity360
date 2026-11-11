package lk.jiat.smartcity360.complaint.chain;

import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;

public final class SupervisorHandler extends AbstractComplaintHandler {
    public SupervisorHandler() {
        super(HandlerLevel.SUPERVISOR, "Reviews escalated service delays and coordinates local teams.");
    }

    @Override
    protected boolean accepts(ComplaintSeverity severity) {
        return severity == ComplaintSeverity.MEDIUM;
    }
}
