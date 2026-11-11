package lk.jiat.smartcity360.complaint.chain;

import lk.jiat.smartcity360.complaint.model.HandlerLevel;

public record ComplaintEscalation(HandlerLevel from, HandlerLevel to, String action,
                                  String note, boolean finalHandling) {
    public boolean handled() {
        return action.equals("ESCALATED") || action.equals("FINAL_HANDLING");
    }
}
