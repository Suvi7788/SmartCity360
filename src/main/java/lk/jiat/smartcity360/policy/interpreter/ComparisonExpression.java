package lk.jiat.smartcity360.policy.interpreter;

import java.util.Set;

public record ComparisonExpression(String variable, Operator operator, double expected) implements Expression {
    @Override
    public boolean interpret(PolicyContext context) {
        double actual = context.valueOf(variable);
        return switch (operator) {
            case LESS -> actual < expected;
            case LESS_OR_EQUAL -> actual <= expected;
            case EQUAL -> Double.compare(actual, expected) == 0;
            case GREATER_OR_EQUAL -> actual >= expected;
            case GREATER -> actual > expected;
        };
    }

    @Override
    public Set<String> variables() {
        return Set.of(variable);
    }

    public enum Operator {
        LESS, LESS_OR_EQUAL, EQUAL, GREATER_OR_EQUAL, GREATER
    }
}
