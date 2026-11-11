package lk.jiat.smartcity360.policy.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PolicyParser {
    private static final Set<String> VARIABLES = Set.of("NOISE_LEVEL", "PARKING_HOUR");
    private static final Pattern COMPARISON = Pattern.compile(
            "^\\s*([A-Za-z][A-Za-z0-9_]*)\\s*(<=|>=|=|<|>)\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");

    public Expression parse(String source) {
        if (source == null || source.isBlank() || source.length() > 500) {
            throw new IllegalArgumentException("Policy expression must contain 1-500 characters");
        }
        if (source.trim().matches("(?i).*\\s+AND\\s*$")) {
            throw new IllegalArgumentException("Policy expression cannot end with AND");
        }
        String[] parts = source.trim().split("(?i)\\s+AND\\s+");
        List<Expression> expressions = new ArrayList<>();
        for (String part : parts) {
            Matcher matcher = COMPARISON.matcher(part);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid comparison: " + part);
            }
            String variable = matcher.group(1).toUpperCase(Locale.ROOT);
            if (!VARIABLES.contains(variable)) {
                throw new IllegalArgumentException("Unsupported policy variable: " + variable);
            }
            double expected;
            try {
                expected = Double.parseDouble(matcher.group(3));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid numeric value in comparison: " + part, exception);
            }
            if (!Double.isFinite(expected)) {
                throw new IllegalArgumentException("Policy values must be finite numbers");
            }
            expressions.add(new ComparisonExpression(variable, operator(matcher.group(2)), expected));
        }
        return expressions.size() == 1 ? expressions.get(0) : new AndExpression(expressions);
    }

    private ComparisonExpression.Operator operator(String value) {
        return switch (value) {
            case "<" -> ComparisonExpression.Operator.LESS;
            case "<=" -> ComparisonExpression.Operator.LESS_OR_EQUAL;
            case "=" -> ComparisonExpression.Operator.EQUAL;
            case ">=" -> ComparisonExpression.Operator.GREATER_OR_EQUAL;
            case ">" -> ComparisonExpression.Operator.GREATER;
            default -> throw new IllegalArgumentException("Unsupported comparison operator: " + value);
        };
    }
}
