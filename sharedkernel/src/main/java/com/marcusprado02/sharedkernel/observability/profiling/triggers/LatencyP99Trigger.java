package com.marcusprado02.sharedkernel.observability.profiling.triggers;

import java.util.Map;

import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class LatencyP99Trigger implements ProfilingTrigger {
    private final double p99Ms;
    public LatencyP99Trigger(double p99Ms){ this.p99Ms = p99Ms; }
    @Override public EvaluationResult evaluate(ProfilingContext ctx) {
        return ctx.p99LatencyMs >= p99Ms
            ? EvaluationResult.trigger("p99_ge_threshold", Map.of("p99ms", ctx.p99LatencyMs, "thr", p99Ms))
            : EvaluationResult.suppress("latency_ok");
    }
}
