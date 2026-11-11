package lk.jiat.smartcity360.complaint.service;

import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.auth.UserRole;
import lk.jiat.smartcity360.complaint.chain.ComplaintHandlerChain;
import lk.jiat.smartcity360.complaint.chain.ComplaintEscalation;
import lk.jiat.smartcity360.complaint.dao.ComplaintRepository;
import lk.jiat.smartcity360.complaint.model.ComplaintRecord;
import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.model.EscalationRecord;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;
import lk.jiat.smartcity360.auth.dao.UserRepository;

import java.util.List;
import java.util.UUID;

public final class ComplaintService {
    private final ComplaintRepository repository;
    private final UserRepository users;
    private final ComplaintHandlerChain handlers = new ComplaintHandlerChain();

    public ComplaintService(ComplaintRepository repository, UserRepository users) {
        this.repository = repository;
        this.users = users;
    }

    public long submit(AuthenticatedUser actor, String description, ComplaintSeverity severity) {
        requireRole(actor, UserRole.RESIDENT);
        String cleanDescription = description == null ? "" : description.trim();
        if (cleanDescription.length() < 8 || cleanDescription.length() > 2000) {
            throw new IllegalArgumentException("Complaint details must contain 8-2,000 characters");
        }
        if (severity == null) {
            throw new IllegalArgumentException("Select a complaint severity");
        }
        HandlerLevel handler = handlers.handlerFor(severity);
        String reference = "SC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return repository.create(reference, cleanDescription, severity.name(), handler, actor.id(),
                users.findFirstActiveIdByRole(roleFor(handler)));
    }

    public List<ComplaintRecord> all(AuthenticatedUser actor) {
        requireAuthenticated(actor);
        return actor.role() == UserRole.RESIDENT
                ? repository.findAllForResident(actor.id()) : repository.findAll();
    }

    public List<EscalationRecord> history(AuthenticatedUser actor, long id) {
        ComplaintRecord complaint = accessibleComplaint(actor, id);
        return repository.history(complaint.id());
    }

    public ComplaintEscalation escalate(AuthenticatedUser actor, long id, String note) {
        ComplaintRecord complaint = accessibleComplaint(actor, id);
        HandlerLevel current = requireHandlerRole(actor);
        if ("RESOLVED".equals(complaint.status())) {
            throw new IllegalArgumentException("Resolved complaints cannot be escalated");
        }
        if (complaint.currentHandler() != current) {
            throw new IllegalArgumentException("Complaint handler changed; refresh and try again");
        }
        if (assignedToAnotherActiveHandler(complaint, actor)) {
            throw new SecurityException("This complaint is assigned to another handler");
        }
        if (repository.history(id).stream().anyMatch(event -> "FINAL_HANDLING".equals(event.action()))) {
            throw new IllegalArgumentException("Final city-level review has already been recorded");
        }
        ComplaintEscalation escalation = handlers.escalationFrom(current);
        String cleanNote = note == null ? "" : note.trim();
        Long assignee = escalation.to() == null
                ? null : users.findFirstActiveIdByRole(roleFor(escalation.to()));
        repository.recordEscalation(id, escalation, cleanNote.isEmpty() ? escalation.note() : cleanNote,
                assignee, actor);
        return escalation;
    }

    public void resolve(AuthenticatedUser actor, long id) {
        ComplaintRecord complaint = accessibleComplaint(actor, id);
        HandlerLevel current = requireHandlerRole(actor);
        if ("RESOLVED".equals(complaint.status())) {
            throw new IllegalArgumentException("Complaint is already resolved");
        }
        if (complaint.currentHandler() != current) {
            throw new IllegalArgumentException("Complaint handler changed; refresh and try again");
        }
        if (assignedToAnotherActiveHandler(complaint, actor)) {
            throw new SecurityException("This complaint is assigned to another handler");
        }
        repository.resolve(id, current, actor);
    }

    private ComplaintRecord accessibleComplaint(AuthenticatedUser actor, long id) {
        requireAuthenticated(actor);
        ComplaintRecord complaint = repository.find(id);
        if (actor.role() == UserRole.RESIDENT
                && (complaint.residentId() == null || complaint.residentId() != actor.id())) {
            throw new IllegalArgumentException("Complaint does not exist");
        }
        return complaint;
    }

    private HandlerLevel requireHandlerRole(AuthenticatedUser actor) {
        requireAuthenticated(actor);
        HandlerLevel level = actor.role().complaintLevel();
        if (level == null) {
            throw new SecurityException("Residents cannot process complaints");
        }
        return level;
    }

    private void requireRole(AuthenticatedUser actor, UserRole required) {
        requireAuthenticated(actor);
        if (actor.role() != required) {
            throw new SecurityException("Only residents can submit complaints");
        }
    }

    private void requireAuthenticated(AuthenticatedUser actor) {
        if (actor == null) {
            throw new SecurityException("Authentication is required");
        }
    }

    private UserRole roleFor(HandlerLevel level) {
        return switch (level) {
            case LOCAL_STAFF -> UserRole.LOCAL_STAFF;
            case SUPERVISOR -> UserRole.SUPERVISOR;
            case CITY_MANAGER -> UserRole.CITY_MANAGER;
        };
    }

    private boolean assignedToAnotherActiveHandler(ComplaintRecord complaint, AuthenticatedUser actor) {
        if (complaint.assignedUserId() == null || complaint.assignedUserId() == actor.id()) {
            return false;
        }
        var assignee = users.findById(complaint.assignedUserId());
        return assignee != null && assignee.active()
                && assignee.role() == roleFor(complaint.currentHandler());
    }
}
