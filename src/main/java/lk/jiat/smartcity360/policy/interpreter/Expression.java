package lk.jiat.smartcity360.policy.interpreter;

import java.util.Set;

public interface Expression {
    boolean interpret(PolicyContext context);

    Set<String> variables();
}
