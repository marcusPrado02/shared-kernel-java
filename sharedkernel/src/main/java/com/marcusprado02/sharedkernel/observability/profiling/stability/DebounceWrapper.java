package com.marcusprado02.sharedkernel.observability.profiling.stability;


import java.time.*;
import java.util.concurrent.atomic.AtomicReference;

import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class DebounceWrapper implements ProfilingTrigger {
    private final ProfilingTrigger delegate;
    private final Duration stableFor;
    private final AtomicReference<Instant> lastTriggerCandidate = new AtomicReference<>(null);

    public DebounceWrapper(ProfilingTrigger d, Duration stableFor) {
        this.delegate = d; this.stableFor = stableFor;
    }
    @Override public EvaluationResult evaluate(ProfilingContext ctx) {
        var res = delegate.evaluate(ctx);
        if (res.decision() == Decision.TRIGGER) {
            var first = lastTriggerCandidate.updateAndGet(prev -> prev==null?ctx.now:prev);
            if (Duration.between(first, ctx.now).compareTo(stableFor) >= 0) {
                lastTriggerCandidate.set(null);
                return res;
            }
            return EvaluationResult.suppress("debouncing_until_" + first.plus(stableFor));
        } else {
            lastTriggerCandidate.set(null);
            return res;
        }
    }
}
