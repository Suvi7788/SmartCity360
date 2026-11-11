package lk.jiat.smartcity360.complaint.service;

import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.auth.UserAccount;
import lk.jiat.smartcity360.auth.UserRole;
import lk.jiat.smartcity360.auth.UserSummary;
import lk.jiat.smartcity360.auth.dao.UserRepository;
import lk.jiat.smartcity360.complaint.chain.ComplaintEscalation;
import lk.jiat.smartcity360.complaint.dao.ComplaintRepository;
import lk.jiat.smartcity360.complaint.model.ComplaintRecord;
import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.model.EscalationRecord;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComplaintServiceTest {
    private static final AuthenticatedUser RESIDENT =
            new AuthenticatedUser(1, "resident", "Resident", UserRole.RESIDENT);
    private static final AuthenticatedUser LOCAL_STAFF =
            new AuthenticatedUser(2, "staff", "Local Staff", UserRole.LOCAL_STAFF);
    private static final AuthenticatedUser SUPERVISOR =
            new AuthenticatedUser(3, "supervisor", "Supervisor", UserRole.SUPERVISOR);
    private static final AuthenticatedUser CITY_MANAGER =
            new AuthenticatedUser(4, "manager", "City Manager", UserRole.CITY_MANAGER);

    @Test
    void persistsEachEscalationAndFinalHandlingThroughTheRepository() {
        TestRepository repository = new TestRepository();
        ComplaintService service = new ComplaintService(repository, new TestUsers());

        ComplaintEscalation first = service.escalate(LOCAL_STAFF, 1, "Needs review");
        ComplaintEscalation second = service.escalate(SUPERVISOR, 1, "");
        ComplaintEscalation finalHandling = service.escalate(CITY_MANAGER, 1, null);

        assertEquals(HandlerLevel.SUPERVISOR, first.to());
        assertEquals(HandlerLevel.CITY_MANAGER, second.to());
        assertEquals("ESCALATED", repository.actions.get(0).action());
        assertEquals("Needs review", repository.notes.get(0));
        assertEquals("FINAL_HANDLING", finalHandling.action());
        assertEquals("CITY_MANAGER", repository.complaint.currentHandler().name());
        assertEquals("IN_PROGRESS", repository.complaint.status());
        assertEquals(3, repository.actions.size());
        assertEquals(finalHandling.note(), repository.notes.get(2));
        assertEquals(List.of(3L, 4L, 4L), repository.assignedUserIds);
    }

    @Test
    void residentsCanOnlyReadTheirOwnComplaintsAndCannotProcessCases() {
        ComplaintService service = new ComplaintService(new TestRepository(), new TestUsers());

        assertThrows(IllegalArgumentException.class, () -> service.history(
                new AuthenticatedUser(9, "other", "Other Resident", UserRole.RESIDENT), 1));
        assertThrows(SecurityException.class, () -> service.escalate(RESIDENT, 1, "Not allowed"));
    }

    @Test
    void onlyTheCurrentHandlerRoleCanEscalateOrResolve() {
        ComplaintService service = new ComplaintService(new TestRepository(), new TestUsers());

        assertThrows(IllegalArgumentException.class, () -> service.escalate(SUPERVISOR, 1, "Wrong handler"));
        assertThrows(IllegalArgumentException.class, () -> service.resolve(CITY_MANAGER, 1));
    }

    private static final class TestUsers implements UserRepository {
        @Override public UserAccount findByIdentifier(String identifier) { return null; }
        @Override public UserAccount findById(long userId) { return null; }
        @Override public Long findFirstActiveIdByRole(UserRole role) {
            return switch (role) {
                case RESIDENT -> 1L;
                case LOCAL_STAFF -> 2L;
                case SUPERVISOR -> 3L;
                case CITY_MANAGER -> 4L;
            };
        }
        @Override public int countActiveByRole(UserRole role) { return 1; }
        @Override public List<UserSummary> findAll() { return List.of(); }
        @Override public long create(String fullName, String username, String email, String passwordHash, UserRole role) {
            return 0;
        }
        @Override public void setActive(long userId, boolean active) { }
        @Override public void setRole(long userId, UserRole role) { }
        @Override public void updatePassword(long userId, String passwordHash) { }
    }

    private static final class TestRepository implements ComplaintRepository {
        private ComplaintRecord complaint = new ComplaintRecord(1, "SC-1", "A sufficiently detailed complaint",
                ComplaintSeverity.LOW, "OPEN", HandlerLevel.LOCAL_STAFF, Instant.EPOCH, 1L, 2L);
        private final List<ComplaintEscalation> actions = new ArrayList<>();
        private final List<String> notes = new ArrayList<>();
        private final List<Long> assignedUserIds = new ArrayList<>();
        private final List<EscalationRecord> history = new ArrayList<>();

        @Override
        public long create(String reference, String description, String severity, HandlerLevel handler,
                           long residentId, Long assignedUserId) {
            throw new UnsupportedOperationException();
        }

        @Override public List<ComplaintRecord> findAll() { return List.of(complaint); }
        @Override public List<ComplaintRecord> findAllForResident(long residentId) {
            return complaint.residentId() == residentId ? List.of(complaint) : List.of();
        }
        @Override public ComplaintRecord find(long complaintId) { return complaint; }
        @Override public List<EscalationRecord> history(long complaintId) { return List.copyOf(history); }

        @Override
        public void recordEscalation(long complaintId, ComplaintEscalation escalation, String note,
                                     Long assignedUserId, AuthenticatedUser actor) {
            actions.add(escalation);
            notes.add(note);
            assignedUserIds.add(assignedUserId);
            HandlerLevel current = escalation.to() == null ? escalation.from() : escalation.to();
            complaint = new ComplaintRecord(complaint.id(), complaint.referenceNo(), complaint.description(),
                    complaint.severity(), "IN_PROGRESS", current, complaint.createdAt(), complaint.residentId(),
                    assignedUserId);
            history.add(new EscalationRecord(current, escalation.action(), note, Instant.EPOCH, actor.id(), actor.role()));
        }

        @Override
        public void resolve(long complaintId, HandlerLevel current, AuthenticatedUser actor) {
            complaint = new ComplaintRecord(complaint.id(), complaint.referenceNo(), complaint.description(),
                    complaint.severity(), "RESOLVED", current, complaint.createdAt(), complaint.residentId(),
                    complaint.assignedUserId());
        }
    }
}
