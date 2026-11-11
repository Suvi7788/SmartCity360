package lk.jiat.smartcity360.complaint.chain;

import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;

public interface ComplaintHandler {
    HandlerLevel level();

    String responsibility();

    HandlerLevel handlerFor(ComplaintSeverity severity);

    ComplaintEscalation escalate();

    ComplaintEscalation escalationFrom(HandlerLevel current);
}
