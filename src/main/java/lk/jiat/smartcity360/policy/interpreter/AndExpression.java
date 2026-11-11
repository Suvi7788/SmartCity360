package lk.jiat.smartcity360.policy.interpreter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record AndExpression(List<Expression> terms) implements Expression {
    public AndExpression {
        terms = List.copyOf(terms);
        if (terms.isEmpty()) {
            throw new IllegalArgumentException("AND expression must contain at least one comparison");
        }
    }

    @Override
    public boolean interpret(PolicyContext context) {
        return terms.stream().allMatch(expression -> expression.interpret(context));
    }

    @Override
    public Set<String> variables() {
        return terms.stream().flatMap(expression -> expression.variables().stream()).collect(Collectors.toUnmodifiableSet());
    }
}
