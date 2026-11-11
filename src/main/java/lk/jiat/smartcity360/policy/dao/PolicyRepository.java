package lk.jiat.smartcity360.policy.dao;

import lk.jiat.smartcity360.policy.model.CityPolicy;
import lk.jiat.smartcity360.policy.model.PolicyEvaluation;

import java.util.List;
import java.util.Map;

public interface PolicyRepository {
    List<CityPolicy> findAll();

    CityPolicy find(long id);

    long save(Long id, String name, String expression, boolean active);

    void saveEvaluation(Long policyId, Map<String, Double> values, Boolean result, String error);

    List<PolicyEvaluation> recentEvaluations();
}
