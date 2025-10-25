package com.marcusprado02.sharedkernel.observability.profiling.composite;

import java.util.Map;

import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class Composite {
    public static ProfilingTrigger and(ProfilingTrigger a, ProfilingTrigger b) {
        return ctx -> {
            var ra = a.evaluate(ctx);
            var rb = b.evaluate(ctx);
            if (ra.decision()==Decision.TRIGGER && rb.decision()==Decision.TRIGGER)
                return EvaluationResult.trigger("and",
                    Map.of("a", ra.reason(), "b", rb.reason()));
            return EvaluationResult.suppress("and_suppressed");
        };
    }
    public static ProfilingTrigger or(ProfilingTrigger a, ProfilingTrigger b) {
        return ctx -> {
            var ra = a.evaluate(ctx);
            if (ra.decision()==Decision.TRIGGER) return ra;
            var rb = b.evaluate(ctx);
            return rb.decision()==Decision.TRIGGER ? rb : EvaluationResult.suppress("or_suppressed");
        };
    }
    public static ProfilingTrigger not(ProfilingTrigger t) {
        return ctx -> {
            var r = t.evaluate(ctx);
            return r.decision()==Decision.TRIGGER ? EvaluationResult.suppress("not") : EvaluationResult.trigger("not", Map.of());
        };
    }
}