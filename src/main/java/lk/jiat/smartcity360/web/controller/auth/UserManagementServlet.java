package lk.jiat.smartcity360.web.controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.auth.UserRole;
import lk.jiat.smartcity360.auth.service.UserManagementService;
import lk.jiat.smartcity360.web.controller.BaseServlet;

import java.io.IOException;
import java.util.Arrays;

@WebServlet("/user-management")
public final class UserManagementServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("roles", Arrays.stream(UserRole.values()).map(Enum::name).toList());
        load(request);
        render(request, response, "user-management");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("roles", Arrays.stream(UserRole.values()).map(Enum::name).toList());
            UserManagementService service = service();
            AuthenticatedUser actor = actor(request);
            String action = request.getParameter("action");
            switch (action == null ? "" : action) {
                case "create" -> {
                    service.create(actor, request.getParameter("fullName"), request.getParameter("username"),
                            request.getParameter("email"), request.getParameter("password"),
                            parseRole(request.getParameter("role")));
                    response.sendRedirect(request.getContextPath() + "/user-management?success=created");
                    return;
                }
                case "active" -> service.setActive(actor, requiredLong(request.getParameter("userId"), "User"),
                        parseBoolean(request.getParameter("active")));
                case "role" -> service.setRole(actor, requiredLong(request.getParameter("userId"), "User"),
                        parseRole(request.getParameter("role")));
                case "password" -> service.resetPassword(actor, requiredLong(request.getParameter("userId"), "User"),
                        request.getParameter("password"));
                default -> throw new IllegalArgumentException("Select a valid user action");
            }
            response.sendRedirect(request.getContextPath() + "/user-management?success=updated");
        } catch (IllegalArgumentException | SecurityException exception) {
            reportError(request, exception.getMessage());
            load(request);
            render(request, response, "user-management");
        } catch (RuntimeException exception) {
            reportUnexpected(request, exception);
            load(request);
            render(request, response, "user-management");
        }
    }

    private UserManagementService service() {
        UserManagementService service = (UserManagementService)
                getServletContext().getAttribute("userManagementService");
        if (service == null) {
            throw new IllegalStateException("User management service is unavailable");
        }
        return service;
    }

    private AuthenticatedUser actor(HttpServletRequest request) {
        return (AuthenticatedUser) request.getAttribute("authenticatedUser");
    }

    private UserRole parseRole(String role) {
        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Select one of the four supported roles");
        }
    }

    private boolean parseBoolean(String value) {
        if ("true".equals(value)) {
            return true;
        }
        if ("false".equals(value)) {
            return false;
        }
        throw new IllegalArgumentException("Select a valid account status");
    }

    private void load(HttpServletRequest request) {
        request.setAttribute("roles", Arrays.stream(UserRole.values()).map(Enum::name).toList());
        try {
            request.setAttribute("users", service().all(actor(request)));
        } catch (RuntimeException exception) {
            reportUnexpected(request, exception);
        }
        if ("created".equals(request.getParameter("success"))) {
            request.setAttribute("success", "User account created.");
        } else if ("updated".equals(request.getParameter("success"))) {
            request.setAttribute("success", "User account updated.");
        }
    }
}
