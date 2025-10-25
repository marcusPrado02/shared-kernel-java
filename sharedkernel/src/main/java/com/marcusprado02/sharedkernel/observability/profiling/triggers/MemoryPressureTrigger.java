package com.marcusprado02.sharedkernel.observability.profiling.triggers;

import java.util.Map;

import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class MemoryPressureTrigger implements ProfilingTrigger {
    private final double usedRatio; // 0.0..1.0
    public MemoryPressureTrigger(double usedRatio){ this.usedRatio = usedRatio; }
    @Override public EvaluationResult evaluate(ProfilingContext ctx) {
        double ratio = (ctx.maxHeapBytes==0)?0:((double)ctx.usedHeapBytes/ctx.maxHeapBytes);
        return ratio >= usedRatio
            ? EvaluationResult.trigger("heap_ratio_ge_threshold", Map.of("ratio", ratio, "thr", usedRatio))
            : EvaluationResult.suppress("heap_ok");
    }
}