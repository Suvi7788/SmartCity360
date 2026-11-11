package lk.jiat.smartcity360.web.controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.auth.SecuritySession;
import lk.jiat.smartcity360.auth.service.AuthenticationService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@WebServlet("/sign-in")
public final class LoginServlet extends HttpServlet {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession(false) != null
                && request.getSession(false).getAttribute(SecuritySession.USER_ATTRIBUTE) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            AuthenticationService service = (AuthenticationService)
                    getServletContext().getAttribute("authenticationService");
            if (service == null) {
                throw new IllegalStateException("Authentication service is unavailable");
            }
            AuthenticatedUser user = service.authenticate(
                    request.getParameter("identifier"), request.getParameter("password"));
            HttpSession session = request.getSession(true);
            request.changeSessionId();
            session.setAttribute(SecuritySession.USER_ATTRIBUTE, user);
            session.setAttribute(SecuritySession.CSRF_ATTRIBUTE, newCsrfToken());
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        } catch (RuntimeException exception) {
            getServletContext().log("Login request failed", exception);
            request.setAttribute("error", "Sign-in is temporarily unavailable. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }

    private String newCsrfToken() {
        byte[] token = new byte[32];
        RANDOM.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
}
