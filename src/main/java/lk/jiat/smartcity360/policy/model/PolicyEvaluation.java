package lk.jiat.smartcity360.policy.model;

import java.time.Instant;

public record PolicyEvaluation(long id, Long policyId, String policyName, String inputValues,
                               Boolean result, String validationError, Instant evaluatedAt) {
}
