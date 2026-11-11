package lk.jiat.smartcity360.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.jiat.smartcity360.auth.AuthenticatedUser;
import lk.jiat.smartcity360.auth.SecuritySession;
import lk.jiat.smartcity360.auth.UserRole;
import lk.jiat.smartcity360.auth.service.AuthenticationService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@WebFilter("/*")
public final class AuthorizationFilter implements Filter {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<String, Set<UserRole>> ROUTE_ROLES = Map.of(
            "/dashboard", EnumSet.allOf(UserRole.class),
            "/complaints", EnumSet.allOf(UserRole.class),
            "/logout", EnumSet.allOf(UserRole.class),
            "/user-management", Set.of(UserRole.CITY_MANAGER),
            "/security-streams", Set.of(UserRole.CITY_MANAGER),
            "/smart-zones", Set.of(UserRole.CITY_MANAGER),
            "/streetlights", Set.of(UserRole.CITY_MANAGER),
            "/policies", Set.of(UserRole.CITY_MANAGER)
    );

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpRequest.setCharacterEncoding("UTF-8");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        String path = httpRequest.getServletPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/dashboard");
            return;
        }
        if (isPublic(path)) {
            if ("/sign-in".equals(path)) {
                ensureCsrfToken(httpRequest.getSession(true));
                if ("POST".equalsIgnoreCase(httpRequest.getMethod()) && !validCsrf(httpRequest)) {
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid form token");
                    return;
                }
            }
            chain.doFilter(request, response);
            return;
        }
        if (!ROUTE_ROLES.containsKey(path)) {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        AuthenticatedUser user = session == null ? null
                : (AuthenticatedUser) session.getAttribute(SecuritySession.USER_ATTRIBUTE);
        if (user == null) {
            if ("GET".equalsIgnoreCase(httpRequest.getMethod())) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/sign-in?required=1");
            } else {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            }
            return;
        }

        AuthenticatedUser current;
        try {
            AuthenticationService authentication = (AuthenticationService)
                    httpRequest.getServletContext().getAttribute("authenticationService");
            if (authentication == null) {
                throw new IllegalStateException("Authentication service is not initialized");
            }
            current = authentication.revalidate(user);
        } catch (RuntimeException exception) {
            httpRequest.getServletContext().log("Unable to validate authenticated session", exception);
            httpResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Authentication services are temporarily unavailable");
            return;
        }
        if (current == null) {
            session.invalidate();
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/sign-in?expired=1");
            return;
        }
        user = current;
        session.setAttribute(SecuritySession.USER_ATTRIBUTE, user);
        request.setAttribute(SecuritySession.USER_ATTRIBUTE, user);

        Set<UserRole> allowedRoles = ROUTE_ROLES.get(path);
        if (allowedRoles != null && !allowedRoles.contains(user.role())) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to access this page");
            return;
        }
        if ("POST".equalsIgnoreCase(httpRequest.getMethod()) && !validCsrf(httpRequest)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid form token");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        return "/sign-in".equals(path) || path.startsWith("/assets/") || "/favicon.ico".equals(path);
    }

    private void ensureCsrfToken(HttpSession session) {
        if (session.getAttribute(SecuritySession.CSRF_ATTRIBUTE) == null) {
            byte[] token = new byte[32];
            RANDOM.nextBytes(token);
            session.setAttribute(SecuritySession.CSRF_ATTRIBUTE,
                    Base64.getUrlEncoder().withoutPadding().encodeToString(token));
        }
    }

    private boolean validCsrf(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SecuritySession.CSRF_ATTRIBUTE);
        String supplied = request.getParameter(SecuritySession.CSRF_ATTRIBUTE);
        return expected instanceof String expectedToken && supplied != null
                && MessageDigest.isEqual(expectedToken.getBytes(StandardCharsets.UTF_8),
                supplied.getBytes(StandardCharsets.UTF_8));
    }
}
