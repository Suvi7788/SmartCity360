package lk.jiat.smartcity360.web.controller.smartzone;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.jiat.smartcity360.smartzone.composite.ZoneOperation;
import lk.jiat.smartcity360.smartzone.service.SmartZoneService;
import lk.jiat.smartcity360.web.controller.BaseServlet;
import lk.jiat.smartcity360.web.view.ZoneRowView;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/smart-zones")
public final class SmartZoneServlet extends BaseServlet {
    private SmartZoneService service() {
        return (SmartZoneService) getServletContext().getAttribute("smartZoneService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        load(request);
        render(request, response, "smart-zones");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            if ("add".equals(action)) {
                service().add(optionalLong(request.getParameter("parentId"), "Parent zone"),
                        request.getParameter("name"), usageKwh(request.getParameter("usageKwh")));
                response.sendRedirect(request.getContextPath() + "/smart-zones?success=added");
                return;
            }
            if ("execute".equals(action)) {
                long id = requiredLong(request.getParameter("zoneId"), "Zone");
                String operationValue = request.getParameter("operation");
                if (operationValue == null || operationValue.isBlank()) {
                    throw new IllegalArgumentException("Select a zone operation");
                }
                ZoneOperation operation = ZoneOperation.valueOf(operationValue);
                request.setAttribute("operationResults", service().execute(id, operation));
            } else {
                throw new IllegalArgumentException("Select a valid zone action");
            }
        } catch (IllegalArgumentException exception) {
            reportError(request, exception.getMessage());
        } catch (IllegalStateException exception) {
            reportUnexpected(request, exception);
        }
        load(request);
        render(request, response, "smart-zones");
    }

    private void load(HttpServletRequest request) {
        try {
            request.setAttribute("zoneRows", service().rows().stream()
                    .map(ZoneRowView::new)
                    .toList());
        } catch (IllegalStateException exception) {
            reportUnexpected(request, exception);
        }
        if ("added".equals(request.getParameter("success"))) {
            request.setAttribute("success", "Zone added to the hierarchy.");
        }
    }

    private BigDecimal usageKwh(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Usage must be a valid decimal number");
        }
    }
}
