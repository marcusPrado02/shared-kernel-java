package com.marcusprado02.sharedkernel.observability.profiling.triggers;

import java.util.Map;

import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class CpuProcessTrigger implements ProfilingTrigger {
    private final double threshold; // ex.: 0.85 (85%)
    public CpuProcessTrigger(double threshold) { this.threshold = threshold; }
    @Override public EvaluationResult evaluate(ProfilingContext ctx) {
        return ctx.processCpuLoad >= threshold
            ? EvaluationResult.trigger("process_cpu_ge_threshold", Map.of("cpu", ctx.processCpuLoad, "thr", threshold))
            : EvaluationResult.suppress("cpu_ok");
    }
}
