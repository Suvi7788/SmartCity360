package lk.jiat.smartcity360.web.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {
    protected void render(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(request, response);
    }

    protected void reportError(HttpServletRequest request, String message) {
        request.setAttribute("error", message);
    }

    protected void reportUnexpected(HttpServletRequest request, RuntimeException exception) {
        getServletContext().log("SmartCity360 request failed", exception);
        request.setAttribute("error", "The operation could not be completed. Check the server configuration or logs.");
    }

    protected Long optionalLong(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value);
            if (parsed < 1) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a positive number");
        }
    }

    protected long requiredLong(String value, String fieldName) {
        Long parsed = optionalLong(value, fieldName);
        if (parsed == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return parsed;
    }
}
