package lk.jiat.smartcity360.web.controller.dashboard;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.jiat.smartcity360.dashboard.service.DashboardService;
import lk.jiat.smartcity360.web.controller.BaseServlet;
import lk.jiat.smartcity360.web.view.DashboardStatsView;

import java.io.IOException;

@WebServlet("/dashboard")
public final class DashboardServlet extends BaseServlet {
    private static final String[] REQUIRED_SERVICES = {
            "securityStreamService", "smartZoneService", "complaintService",
            "streetlightService", "policyService"
    };

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("webApplicationStatus", "RUNNING");
        request.setAttribute("databaseStatus", "UNAVAILABLE");
        boolean moduleServicesInitialized = requiredServicesAvailable();
        request.setAttribute("requiredServicesAvailable", false);
        if (!moduleServicesInitialized) {
            reportUnexpected(request, new IllegalStateException("One or more required application services are unavailable"));
        }
        try {
            DashboardService service = (DashboardService) getServletContext().getAttribute("dashboardService");
            if (service == null) {
                throw new IllegalStateException("Dashboard service is unavailable");
            }
            request.setAttribute("stats", new DashboardStatsView(service.loadStats()));
            request.setAttribute("databaseStatus", "CONNECTED");
            request.setAttribute("requiredServicesAvailable", moduleServicesInitialized);
        } catch (RuntimeException exception) {
            reportUnexpected(request, exception);
        }
        render(request, response, "dashboard");
    }

    private boolean requiredServicesAvailable() {
        for (String serviceName : REQUIRED_SERVICES) {
            if (getServletContext().getAttribute(serviceName) == null) {
                return false;
            }
        }
        return true;
    }
}
