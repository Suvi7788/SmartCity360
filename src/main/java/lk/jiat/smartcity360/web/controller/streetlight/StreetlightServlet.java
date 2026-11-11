package lk.jiat.smartcity360.web.controller.streetlight;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.jiat.smartcity360.streetlight.model.LightConfiguration;
import lk.jiat.smartcity360.streetlight.service.StreetlightService;
import lk.jiat.smartcity360.web.controller.BaseServlet;
import lk.jiat.smartcity360.web.view.StreetlightView;

import java.io.IOException;

@WebServlet("/streetlights")
public final class StreetlightServlet extends BaseServlet {
    private StreetlightService service() {
        return (StreetlightService) getServletContext().getAttribute("streetlightService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        load(request);
        render(request, response, "streetlights");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int count = Integer.parseInt(request.getParameter("count"));
            LightConfiguration configuration = new LightConfiguration(
                    Integer.parseInt(request.getParameter("wattage")),
                    request.getParameter("brightnessMode"),
                    Integer.parseInt(request.getParameter("colorTemperature")),
                    request.getParameter("sensorProfile"));
            int created = service().createBatch(count, request.getParameter("location"),
                    optionalLong(request.getParameter("zoneId"), "Zone"), configuration);
            response.sendRedirect(request.getContextPath() + "/streetlights?created=" + created);
            return;
        } catch (NumberFormatException exception) {
            reportError(request, "Batch size, wattage, and color temperature must be whole numbers.");
        } catch (IllegalArgumentException exception) {
            reportError(request, exception.getMessage());
        } catch (IllegalStateException exception) {
            reportUnexpected(request, exception);
        }
        load(request);
        render(request, response, "streetlights");
    }

    private void load(HttpServletRequest request) {
        try {
            var service = service();
            request.setAttribute("streetlights", service.recent().stream()
                    .map(StreetlightView::new)
                    .toList());
            request.setAttribute("totalLights", service.totalLights());
            request.setAttribute("uniqueConfigurations", service.uniqueConfigurations());
            request.setAttribute("cachedConfigurations", service.cachedConfigurations());
        } catch (IllegalStateException exception) {
            reportUnexpected(request, exception);
        }
        String created = request.getParameter("created");
        if (created != null) {
            request.setAttribute("success", created + " simulated streetlights created.");
        }
    }
}
