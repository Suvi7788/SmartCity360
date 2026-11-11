package lk.jiat.smartcity360.complaint.chain;

import lk.jiat.smartcity360.complaint.model.ComplaintSeverity;
import lk.jiat.smartcity360.complaint.model.HandlerLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplaintHandlerChainTest {
    @Test
    void routesSeverityAndEscalatesInOrder() {
        ComplaintHandlerChain chain = new ComplaintHandlerChain();

        assertEquals(HandlerLevel.LOCAL_STAFF, chain.handlerFor(ComplaintSeverity.LOW));
        assertEquals(HandlerLevel.SUPERVISOR, chain.handlerFor(ComplaintSeverity.MEDIUM));
        assertEquals(HandlerLevel.CITY_MANAGER, chain.handlerFor(ComplaintSeverity.HIGH));
        assertTrue(chain.responsibility().contains("routine"));

        assertEquals(HandlerLevel.SUPERVISOR,
                chain.escalationFrom(HandlerLevel.LOCAL_STAFF).to());
        assertEquals(HandlerLevel.CITY_MANAGER,
                chain.escalationFrom(HandlerLevel.SUPERVISOR).to());
        var finalHandling = chain.escalationFrom(HandlerLevel.CITY_MANAGER);
        assertEquals("FINAL_HANDLING", finalHandling.action());
        assertTrue(finalHandling.finalHandling());
        assertTrue(finalHandling.handled());

        var unhandled = chain.escalationFrom(null);
        assertEquals("UNHANDLED", unhandled.action());
        assertFalse(unhandled.handled());
        assertFalse(unhandled.finalHandling());
    }
}
