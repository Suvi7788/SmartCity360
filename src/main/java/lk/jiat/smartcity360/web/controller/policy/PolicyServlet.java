package lk.jiat.smartcity360.web.controller.policy;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.jiat.smartcity360.policy.service.PolicyService;
import lk.jiat.smartcity360.web.controller.BaseServlet;
import lk.jiat.smartcity360.web.view.CityPolicyView;
import lk.jiat.smartcity360.web.view.PolicyEvaluationView;

import java.io.IOException;

@WebServlet("/policies")
public final class PolicyServlet extends BaseServlet {
    private PolicyService service() {
        return (PolicyService) getServletContext().getAttribute("policyService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        load(request);
        render(request, response, "policies");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            if ("save".equals(action)) {
                long id = service().save(optionalLong(request.getParameter("policyId"), "Policy"),
                        request.getParameter("name"), request.getParameter("expression"),
                        "true".equals(request.getParameter("active")));
                response.sendRedirect(request.getContextPath() + "/policies?saved=" + id);
                return;
            }
            if ("evaluate".equals(action)) {
                boolean result = service().evaluate(requiredLong(request.getParameter("policyId"), "Policy"),
                        request.getParameter("inputValues"));
                request.setAttribute("success", "Policy evaluation: " + result);
            } else {
                throw new IllegalArgumentException("Select a valid policy action");
            }
        } catch (IllegalArgumentException exception) {
            reportError(request, exception.getMessage());
        } catch (IllegalStateException exception) {
            reportUnexpected(request, exception);
        }
        load(request);
        render(request, response, "policies");
    }

    private void load(HttpServletRequest request) {
        try {
            var service = service();
            var domainPolicies = service.policies();
            var policies = domainPolicies.stream()
                    .map(CityPolicyView::new)
                    .toList();
            request.setAttribute("policies", policies);
            request.setAttribute("evaluations", service.recentEvaluations().stream()
                    .map(PolicyEvaluationView::new)
                    .toList());
            String editId = request.getParameter("edit");
            if (editId != null && !editId.isBlank()) {
                long id = requiredLong(editId, "Policy");
                request.setAttribute("editingPolicy", domainPolicies.stream()
                        .filter(policy -> policy.id() == id)
                        .findFirst()
                        .map(CityPolicyView::new)
                        .orElseThrow(() -> new IllegalArgumentException("Policy does not exist")));
            }
        } catch (IllegalStateException exception) {
            reportUnexpected(request, exception);
        } catch (IllegalArgumentException exception) {
            reportError(request, exception.getMessage());
        }
        if (request.getParameter("saved") != null) {
            request.setAttribute("success", "Policy saved.");
        }
    }
}
