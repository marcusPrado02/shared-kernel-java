package com.marcusprado02.sharedkernel.observability.profiling.stability;

import java.time.*;
import java.util.concurrent.atomic.AtomicReference;

import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class CooldownWrapper implements ProfilingTrigger {
    private final ProfilingTrigger delegate;
    private final Duration cooldown;
    private volatile Instant nextAllowed = Instant.EPOCH;
    public CooldownWrapper(ProfilingTrigger delegate, Duration cooldown) {
        this.delegate = delegate; this.cooldown = cooldown;
    }
    @Override public EvaluationResult evaluate(ProfilingContext ctx) {
        if (ctx.now.isBefore(nextAllowed))
            return EvaluationResult.suppress("cooldown_active_until_" + nextAllowed);
        var res = delegate.evaluate(ctx);
        if (res.decision() == Decision.TRIGGER) {
            nextAllowed = ctx.now.plus(cooldown);
        }
        return res;
    }
}
