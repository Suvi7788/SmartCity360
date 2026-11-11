package lk.jiat.smartcity360.complaint.chain;

import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;

public final class ComplaintHandlerChain implements ComplaintHandler {
    private final LocalStaffHandler first = new LocalStaffHandler();

    public ComplaintHandlerChain() {
        SupervisorHandler supervisor = new SupervisorHandler();
        CityManagerHandler manager = new CityManagerHandler();
        first.setNext(supervisor);
        supervisor.setNext(manager);
    }

    @Override
    public HandlerLevel level() {
        return first.level();
    }

    @Override
    public String responsibility() {
        return first.responsibility();
    }

    @Override
    public HandlerLevel handlerFor(ComplaintSeverity severity) {
        return first.handlerFor(severity);
    }

    @Override
    public ComplaintEscalation escalate() {
        return first.escalate();
    }

    @Override
    public ComplaintEscalation escalationFrom(HandlerLevel current) {
        return first.escalationFrom(current);
    }
}
