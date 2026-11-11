package lk.jiat.smartcity360.web.controller.complaint;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.service.ComplaintService;
import lk.jiat.smartcity360.web.controller.BaseServlet;
import lk.jiat.smartcity360.web.view.ComplaintView;
import lk.jiat.smartcity360.web.view.EscalationView;

import java.io.IOException;

@WebServlet("/complaints")
public final class ComplaintServlet extends BaseServlet {
    private ComplaintService service() {
        return (ComplaintService) getServletContext().getAttribute("complaintService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        load(request, authenticatedUser(request));
        render(request, response, "complaints");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            AuthenticatedUser actor = authenticatedUser(request);
            String action = request.getParameter("action");
            if ("submit".equals(action)) {
                String severityValue = request.getParameter("severity");
                if (severityValue == null || severityValue.isBlank()) {
                    throw new IllegalArgumentException("Select a complaint severity");
                }
                service().submit(actor, request.getParameter("description"),
                        ComplaintSeverity.valueOf(severityValue));
                response.sendRedirect(request.getContextPath() + "/complaints?success=submitted");
                return;
            }
            long id = requiredLong(request.getParameter("complaintId"), "Complaint");
            if ("escalate".equals(action)) {
                service().escalate(actor, id, request.getParameter("note"));
            } else if ("resolve".equals(action)) {
                service().resolve(actor, id);
            } else {
                throw new IllegalArgumentException("Select a valid complaint action");
            }
            response.sendRedirect(request.getContextPath() + "/complaints?selected=" + id);
            return;
        } catch (IllegalArgumentException | SecurityException exception) {
            reportError(request, exception.getMessage());
        } catch (IllegalStateException exception) {
            reportUnexpected(request, exception);
        }
        load(request, authenticatedUser(request));
        render(request, response, "complaints");
    }

    private void load(HttpServletRequest request, AuthenticatedUser actor) {
        try {
            var service = service();
            request.setAttribute("complaints", service.all(actor).stream()
                    .map(ComplaintView::new)
                    .toList());
            String selected = request.getParameter("selected");
            if (selected != null && !selected.isBlank()) {
                long id = requiredLong(selected, "Complaint");
                request.setAttribute("selectedComplaintId", id);
                request.setAttribute("history", service.history(actor, id).stream()
                        .map(EscalationView::new)
                        .toList());
            }
        } catch (IllegalArgumentException exception) {
            reportError(request, exception.getMessage());
        } catch (IllegalStateException exception) {
            reportUnexpected(request, exception);
        }
        if ("submitted".equals(request.getParameter("success"))) {
            request.setAttribute("success", "Complaint submitted and routed to its initial handler.");
        }
    }

    private AuthenticatedUser authenticatedUser(HttpServletRequest request) {
        return (AuthenticatedUser) request.getAttribute("authenticatedUser");
    }
}
