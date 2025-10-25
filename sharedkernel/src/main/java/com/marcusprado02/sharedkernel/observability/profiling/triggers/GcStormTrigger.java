package com.marcusprado02.sharedkernel.observability.profiling.triggers;

import java.util.Map;

import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class GcStormTrigger implements ProfilingTrigger {
    private final long youngPerMin; private final long fullPer5Min;
    public GcStormTrigger(long youngPerMin, long fullPer5Min){
        this.youngPerMin = youngPerMin; this.fullPer5Min = fullPer5Min;
    }
    @Override public EvaluationResult evaluate(ProfilingContext ctx) {
        if (ctx.youngGcCount1m >= youngPerMin || ctx.fullGcCount5m >= fullPer5Min)
            return EvaluationResult.trigger("gc_storm", Map.of("ygc1m", ctx.youngGcCount1m, "fgc5m", ctx.fullGcCount5m));
        return EvaluationResult.suppress("gc_normal");
    }
}