package com.marcusprado02.sharedkernel.observability.profiling;

import java.util.Map;

public record EvaluationResult(Decision decision, String reason, Map<String,Object> attributes) {
    public static EvaluationResult trigger(String reason, Map<String,Object> attrs){
        return new EvaluationResult(Decision.TRIGGER, reason, attrs == null? Map.of():Map.copyOf(attrs));
    }
    public static EvaluationResult suppress(String reason){
        return new EvaluationResult(Decision.SUPPRESS, reason, Map.of());
    }
}