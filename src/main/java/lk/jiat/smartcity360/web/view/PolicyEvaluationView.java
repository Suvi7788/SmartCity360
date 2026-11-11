package lk.jiat.smartcity360.web.view;

import lk.jiat.smartcity360.policy.model.PolicyEvaluation;

import java.time.Instant;

public final class PolicyEvaluationView {
    private final long id;
    private final Long policyId;
    private final String policyName;
    private final String inputValues;
    private final Boolean result;
    private final String validationError;
    private final Instant evaluatedAt;

    public PolicyEvaluationView(PolicyEvaluation evaluation) {
        this.id = evaluation.id();
        this.policyId = evaluation.policyId();
        this.policyName = evaluation.policyName();
        this.inputValues = evaluation.inputValues();
        this.result = evaluation.result();
        this.validationError = evaluation.validationError();
        this.evaluatedAt = evaluation.evaluatedAt();
    }

    public long getId() {
        return id;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public String getInputValues() {
        return inputValues;
    }

    public Boolean getResult() {
        return result;
    }

    public String getValidationError() {
        return validationError;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }
}
