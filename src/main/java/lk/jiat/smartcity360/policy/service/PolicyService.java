package lk.jiat.smartcity360.policy.service;

import lk.jiat.smartcity360.policy.dao.PolicyRepository;
import lk.jiat.smartcity360.policy.interpreter.Expression;
import lk.jiat.smartcity360.policy.interpreter.PolicyContext;
import lk.jiat.smartcity360.policy.interpreter.PolicyParser;
import lk.jiat.smartcity360.policy.model.CityPolicy;
import lk.jiat.smartcity360.policy.model.PolicyEvaluation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class PolicyService {
    private final PolicyRepository repository;
    private final PolicyParser parser = new PolicyParser();
    private static final Set<String> ALLOWED_VARIABLES = Set.of("NOISE_LEVEL", "PARKING_HOUR");

    public PolicyService(PolicyRepository repository) {
        this.repository = repository;
    }

    public long save(Long id, String name, String expression, boolean active) {
        String cleanName = name == null ? "" : name.trim();
        if (cleanName.isEmpty() || cleanName.length() > 120) {
            throw new IllegalArgumentException("Policy name must contain 1-120 characters");
        }
        String cleanExpression = expression == null ? "" : expression.trim();
        parser.parse(cleanExpression);
        boolean duplicateName = repository.findAll().stream()
                .anyMatch(policy -> (id == null || policy.id() != id)
                        && policy.name().equalsIgnoreCase(cleanName));
        if (duplicateName) {
            throw new IllegalArgumentException("A policy with this name already exists");
        }
        return repository.save(id, cleanName, cleanExpression, active);
    }

    public List<CityPolicy> policies() {
        return repository.findAll();
    }

    public List<PolicyEvaluation> recentEvaluations() {
        return repository.recentEvaluations();
    }

    public boolean evaluate(long policyId, String inputText) {
        CityPolicy policy = repository.find(policyId);
        Map<String, Double> values = new LinkedHashMap<>();
        Boolean result = null;
        String error = null;
        try {
            if (!policy.active()) {
                throw new IllegalArgumentException("This policy is inactive");
            }
            Expression expression = parser.parse(policy.expression());
            values = parseValues(inputText);
            if (!values.keySet().containsAll(expression.variables())) {
                throw new IllegalArgumentException("Provide a value for every variable used in the policy");
            }
            result = expression.interpret(new PolicyContext(values));
        } catch (IllegalArgumentException exception) {
            error = exception.getMessage();
        }
        repository.saveEvaluation(policyId, values, result, error);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        return result;
    }

    private Map<String, Double> parseValues(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            throw new IllegalArgumentException("Enter values such as NOISE_LEVEL=55;PARKING_HOUR=9");
        }
        if (inputText.length() > 500) {
            throw new IllegalArgumentException("Policy input values must not exceed 500 characters");
        }
        Map<String, Double> values = new LinkedHashMap<>();
        for (String item : inputText.split(";")) {
            String[] pair = item.trim().split("=", -1);
            if (pair.length != 2) {
                throw new IllegalArgumentException("Invalid input value: " + item);
            }
            String key = pair[0].trim().toUpperCase(Locale.ROOT);
            if (!ALLOWED_VARIABLES.contains(key)) {
                throw new IllegalArgumentException("Unsupported input variable: " + key);
            }
            try {
                double value = Double.parseDouble(pair[1].trim());
                if (!Double.isFinite(value)) {
                    throw new IllegalArgumentException("Input values must be finite numbers");
                }
                if (values.putIfAbsent(key, value) != null) {
                    throw new IllegalArgumentException("Duplicate input variable: " + key);
                }
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid number for " + key, exception);
            }
        }
        return values;
    }
}
