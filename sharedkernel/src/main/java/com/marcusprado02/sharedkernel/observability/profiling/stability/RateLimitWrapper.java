package com.marcusprado02.sharedkernel.observability.profiling.stability;


import java.time.*;
import java.util.concurrent.atomic.AtomicReference;
import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class RateLimitWrapper implements ProfilingTrigger {
    private final ProfilingTrigger delegate;
    private final int maxPerHour;
    private int count = 0;
    private Instant windowStart = Instant.EPOCH;

    public RateLimitWrapper(ProfilingTrigger d, int maxPerHour) {
        this.delegate = d; this.maxPerHour = maxPerHour;
    }
    @Override public synchronized EvaluationResult evaluate(ProfilingContext ctx) {
        if (Duration.between(windowStart, ctx.now).toHours() >= 1) {
            windowStart = ctx.now; count = 0;
        }
        var res = delegate.evaluate(ctx);
        if (res.decision() == Decision.TRIGGER) {
            if (count >= maxPerHour) return EvaluationResult.suppress("rate_limited");
            count++;
        }
        return res;
    }
}
