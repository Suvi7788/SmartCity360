package lk.jiat.smartcity360.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lk.jiat.smartcity360.complaint.dao.JdbcComplaintRepository;
import lk.jiat.smartcity360.complaint.service.ComplaintService;
import lk.jiat.smartcity360.auth.BCryptPasswordHasher;
import lk.jiat.smartcity360.auth.dao.JdbcUserRepository;
import lk.jiat.smartcity360.auth.service.AuthenticationService;
import lk.jiat.smartcity360.auth.service.UserManagementService;
import lk.jiat.smartcity360.dashboard.dao.DashboardRepository;
import lk.jiat.smartcity360.dashboard.service.DashboardService;
import lk.jiat.smartcity360.policy.dao.JdbcPolicyRepository;
import lk.jiat.smartcity360.policy.service.PolicyService;
import lk.jiat.smartcity360.securitystream.dao.JdbcStreamRunRepository;
import lk.jiat.smartcity360.securitystream.service.SecurityStreamService;
import lk.jiat.smartcity360.smartzone.dao.JdbcZoneRepository;
import lk.jiat.smartcity360.smartzone.service.SmartZoneService;
import lk.jiat.smartcity360.streetlight.dao.JdbcStreetlightRepository;
import lk.jiat.smartcity360.streetlight.flyweight.StreetlightFactory;
import lk.jiat.smartcity360.streetlight.service.StreetlightService;

@WebListener
public final class AppBootstrap implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        var context = event.getServletContext();
        var users = new JdbcUserRepository();
        var passwordHasher = new BCryptPasswordHasher();
        context.setAttribute("userRepository", users);
        context.setAttribute("authenticationService", new AuthenticationService(users, passwordHasher));
        context.setAttribute("userManagementService", new UserManagementService(users, passwordHasher));
        context.setAttribute("dashboardService", new DashboardService(new DashboardRepository()));
        context.setAttribute("securityStreamService",
                new SecurityStreamService(new JdbcStreamRunRepository()));
        context.setAttribute("smartZoneService", new SmartZoneService(new JdbcZoneRepository()));
        context.setAttribute("complaintService",
                new ComplaintService(new JdbcComplaintRepository(), users));
        context.setAttribute("streetlightService",
                new StreetlightService(new JdbcStreetlightRepository(), new StreetlightFactory()));
        context.setAttribute("policyService", new PolicyService(new JdbcPolicyRepository()));
    }

}
