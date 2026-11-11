package lk.jiat.smartcity360.web.view;

import lk.jiat.smartcity360.policy.model.CityPolicy;

public final class CityPolicyView {
    private final long id;
    private final String name;
    private final String expression;
    private final boolean active;

    public CityPolicyView(CityPolicy policy) {
        this.id = policy.id();
        this.name = policy.name();
        this.expression = policy.expression();
        this.active = policy.active();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    public boolean isActive() {
        return active;
    }
}
