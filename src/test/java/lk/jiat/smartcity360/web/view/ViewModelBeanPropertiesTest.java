package lk.jiat.smartcity360.web.view;

import lk.jiat.smartcity360.complaint.model.ComplaintRecord;
import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.model.EscalationRecord;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;
import lk.jiat.smartcity360.dashboard.model.DashboardStats;
import lk.jiat.smartcity360.policy.model.CityPolicy;
import lk.jiat.smartcity360.policy.model.PolicyEvaluation;
import lk.jiat.smartcity360.securitystream.model.StreamRun;
import lk.jiat.smartcity360.smartzone.model.ZoneType;
import lk.jiat.smartcity360.smartzone.model.ZoneViewRow;
import lk.jiat.smartcity360.streetlight.model.LightConfiguration;
import lk.jiat.smartcity360.streetlight.model.Streetlight;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.beans.Introspector;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewModelBeanPropertiesTest {
    @Test
    void exposesEveryJspViewModelPropertyThroughJavaBeansGetters() throws Exception {
        assertProperties(DashboardStatsView.class, "streamRuns", "zones", "openComplaints",
                "streetlights", "activePolicies");
        assertProperties(StreamRunView.class, "id", "meterId", "decoratorChain",
                "originalPayload", "processedPayload", "createdAt");
        assertProperties(ZoneRowView.class, "id", "parentId", "type", "name", "depth",
                "hasChildren", "canHaveChildren");
        assertProperties(ComplaintView.class, "id", "referenceNo", "description", "severity",
                "status", "currentHandler", "createdAt");
        assertProperties(EscalationView.class, "handler", "action", "note", "occurredAt");
        assertProperties(StreetlightView.class, "code", "zoneId", "location", "status", "configuration");
        assertProperties(LightConfigurationView.class, "wattage", "brightnessMode",
                "colorTemperature", "sensorProfile");
        assertProperties(CityPolicyView.class, "id", "name", "expression", "active");
        assertProperties(PolicyEvaluationView.class, "id", "policyId", "policyName",
                "inputValues", "result", "validationError", "evaluatedAt");
    }

    @Test
    void mapsDomainRecordsIntoJspViewModels() {
        Instant now = Instant.parse("2026-11-08T10:00:00Z");
        DashboardStatsView stats = new DashboardStatsView(new DashboardStats(1, 2, 3, 4, 5));
        StreamRunView run = new StreamRunView(new StreamRun(6, "MTR-1", "Compression",
                "original", "processed", now));
        ZoneRowView zone = new ZoneRowView(new ZoneViewRow(7, 8L, ZoneType.BUILDING, "Library",
                3, false, true, BigDecimal.ZERO));
        ComplaintView complaint = new ComplaintView(new ComplaintRecord(9, "SC-9", "Description",
                ComplaintSeverity.HIGH, "OPEN", HandlerLevel.CITY_MANAGER, now));
        EscalationView escalation = new EscalationView(new EscalationRecord(
                HandlerLevel.SUPERVISOR, "ESCALATED", "Review", now));
        StreetlightView streetlight = new StreetlightView(new Streetlight("SL-1", 10L,
                "Main / Pole 1", "ON", new LightConfiguration(80, "Adaptive", 4000, "Standard")));
        CityPolicyView policy = new CityPolicyView(new CityPolicy(11, "Noise", "NOISE_LEVEL <= 60", true));
        PolicyEvaluationView evaluation = new PolicyEvaluationView(new PolicyEvaluation(
                12, 11L, "Noise", "{\"NOISE_LEVEL\":55}", true, null, now));

        assertEquals(1, stats.getStreamRuns());
        assertEquals("processed", run.getProcessedPayload());
        assertEquals("BUILDING", zone.getType());
        assertEquals(false, zone.isCanHaveChildren());
        assertEquals("SC-9", complaint.getReferenceNo());
        assertEquals("SUPERVISOR", escalation.getHandler());
        assertEquals("Adaptive", streetlight.getConfiguration().getBrightnessMode());
        assertEquals("NOISE_LEVEL <= 60", policy.getExpression());
        assertEquals(true, evaluation.getResult());
    }

    private void assertProperties(Class<?> beanType, String... expected) throws Exception {
        Set<String> properties = Arrays.stream(Introspector.getBeanInfo(beanType).getPropertyDescriptors())
                .map(descriptor -> descriptor.getName())
                .collect(Collectors.toSet());

        for (String property : expected) {
            assertTrue(properties.contains(property),
                    () -> beanType.getSimpleName() + " is missing JavaBean property '" + property + "'");
        }
    }
}
