package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api;

import java.util.Map;

public record EvaluationDetail<T>(
    T value,
    String flagKey,
    String variant,               // ex.: "control", "treatment"
    String reason,                // ex.: "RULE_MATCH:rule-2", "OFF:fallthrough"
    boolean isDefault,
    Map<String, Object> metadata  // ex.: ruleId, prerequisites
) {}
