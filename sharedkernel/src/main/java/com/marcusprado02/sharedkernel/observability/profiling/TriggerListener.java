package com.marcusprado02.sharedkernel.observability.profiling;

public interface TriggerListener {
    void onDecision(String triggerName, EvaluationResult result, ProfilingContext ctx);
}