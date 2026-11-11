package lk.jiat.smartcity360.policy.interpreter;

import java.util.Locale;
import java.util.Map;

public final class PolicyContext {
    private final Map<String, Double> values;

    public PolicyContext(Map<String, Double> values) {
        this.values = values.entrySet().stream().collect(
                java.util.stream.Collectors.toUnmodifiableMap(
                        entry -> entry.getKey().toUpperCase(Locale.ROOT), Map.Entry::getValue));
    }

    public double valueOf(String variable) {
        Double value = values.get(variable.toUpperCase(Locale.ROOT));
        if (value == null) {
            throw new IllegalArgumentException("No value supplied for " + variable);
        }
        return value;
    }
}
