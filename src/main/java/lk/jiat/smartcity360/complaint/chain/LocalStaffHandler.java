package lk.jiat.smartcity360.complaint.chain;

import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;

public final class LocalStaffHandler extends AbstractComplaintHandler {
    public LocalStaffHandler() {
        super(HandlerLevel.LOCAL_STAFF, "Handles routine neighborhood service issues and first response.");
    }

    @Override
    protected boolean accepts(ComplaintSeverity severity) {
        return severity == ComplaintSeverity.LOW;
    }
}
