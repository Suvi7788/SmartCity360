package lk.jiat.smartcity360.policy.interpreter;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyParserTest {
    private final PolicyParser parser = new PolicyParser();

    @Test
    void parsesComparisonsJoinedByAnd() {
        Expression expression = parser.parse(
                "NOISE_LEVEL <= 60 AND PARKING_HOUR >= 8 AND PARKING_HOUR <= 18");

        assertTrue(expression.interpret(new PolicyContext(Map.of("NOISE_LEVEL", 45.0, "PARKING_HOUR", 9.0))));
        assertFalse(expression.interpret(new PolicyContext(Map.of("NOISE_LEVEL", 61.0, "PARKING_HOUR", 9.0))));
    }

    @Test
    void rejectsUnknownVariablesAndIncompleteBooleanExpressions() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("TRAFFIC_LEVEL <= 4"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("NOISE_LEVEL <= 60 AND"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("NOISE_LEVEL <= 60 OR PARKING_HOUR > 8"));
    }
}
