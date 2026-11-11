package lk.jiat.smartcity360.web.controller.securitystream;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.jiat.smartcity360.securitystream.service.SecurityStreamService;
import lk.jiat.smartcity360.web.controller.BaseServlet;
import lk.jiat.smartcity360.web.view.StreamRunView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/security-streams")
public final class SecurityStreamServlet extends BaseServlet {
    private SecurityStreamService service() {
        return (SecurityStreamService) getServletContext().getAttribute("securityStreamService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        load(request);
        render(request, response, "security-streams");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            double energy = Double.parseDouble(request.getParameter("energyKwh"));
            String[] decorators = request.getParameterValues("decorator");
            service().process(request.getParameter("meterId"), energy,
                    decorators == null ? List.of() : Arrays.asList(decorators));
            response.sendRedirect(request.getContextPath() + "/security-streams?success=processed");
        } catch (NumberFormatException exception) {
            reportError(request, "Energy usage must be a number.");
            load(request);
            render(request, response, "security-streams");
        } catch (IllegalArgumentException exception) {
            reportError(request, exception.getMessage());
            load(request);
            render(request, response, "security-streams");
        } catch (IllegalStateException exception) {
            reportUnexpected(request, exception);
            load(request);
            render(request, response, "security-streams");
        }
    }

    private void load(HttpServletRequest request) {
        try {
            request.setAttribute("runs", service().recentRuns().stream()
                    .map(StreamRunView::new)
                    .toList());
        } catch (IllegalStateException exception) {
            reportUnexpected(request, exception);
        }
        if ("processed".equals(request.getParameter("success"))) {
            request.setAttribute("success", "Meter stream processed and saved.");
        }
    }
}
