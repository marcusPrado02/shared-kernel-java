package com.marcusprado02.sharedkernel.observability.profiling.triggers;

import java.util.Map;

import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class ErrorRateTrigger implements ProfilingTrigger {
    private final double errorsPerMin;
    public ErrorRateTrigger(double errorsPerMin){ this.errorsPerMin = errorsPerMin; }
    @Override public EvaluationResult evaluate(ProfilingContext ctx) {
        return ctx.errorRatePerMin >= errorsPerMin
            ? EvaluationResult.trigger("error_rate_ge_threshold", Map.of("err_per_min", ctx.errorRatePerMin, "thr", errorsPerMin))
            : EvaluationResult.suppress("errors_ok");
    }
}