package lk.jiat.smartcity360.complaint.dao;

import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.complaint.model.ComplaintRecord;
import lk.jiat.smartcity360.complaint.model.EscalationRecord;
import lk.jiat.smartcity360.complaint.chain.ComplaintEscalation;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;

import java.util.List;

public interface ComplaintRepository {
    long create(String reference, String description, String severity, HandlerLevel handler,
                long residentId, Long assignedUserId);

    List<ComplaintRecord> findAll();

    List<ComplaintRecord> findAllForResident(long residentId);

    ComplaintRecord find(long complaintId);

    List<EscalationRecord> history(long complaintId);

    void recordEscalation(long complaintId, ComplaintEscalation escalation, String note,
                          Long assignedUserId, AuthenticatedUser actor);

    void resolve(long complaintId, HandlerLevel current, AuthenticatedUser actor);
}
