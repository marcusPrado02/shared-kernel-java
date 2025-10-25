package com.marcusprado02.sharedkernel.crosscutting.hook.around;

import com.marcusprado02.sharedkernel.crosscutting.hook.*;

public final class TimingAround<I,O> implements Around<I,O> {
    private final Telemetry t;
    public TimingAround(Telemetry t){ this.t=t==null?Telemetry.NOOP:t; }
    @Override public O apply(I input, HookContext ctx, Proceed<I,O> next) throws Exception {
        return t.time("callback.latency", () -> next.call(input, ctx), java.util.Map.of("topic",ctx.topic()));
    }
}
