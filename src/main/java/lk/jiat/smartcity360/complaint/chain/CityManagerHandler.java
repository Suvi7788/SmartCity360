package lk.jiat.smartcity360.complaint.chain;

import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;

public final class CityManagerHandler extends AbstractComplaintHandler {
    public CityManagerHandler() {
        super(HandlerLevel.CITY_MANAGER, "Provides final city-level review and prioritization.");
    }

    @Override
    protected boolean accepts(ComplaintSeverity severity) {
        return severity == ComplaintSeverity.HIGH;
    }
}
