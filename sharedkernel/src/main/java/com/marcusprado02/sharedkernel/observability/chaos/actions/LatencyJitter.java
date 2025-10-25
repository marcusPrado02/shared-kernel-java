package com.marcusprado02.sharedkernel.observability.chaos.actions;

import java.util.concurrent.ThreadLocalRandom;

import com.marcusprado02.sharedkernel.observability.chaos.*;

public final class LatencyJitter implements ChaosAction {
    private final long minMs, maxMs;
    public LatencyJitter(long minMs, long maxMs){ this.minMs=Math.max(0,minMs); this.maxMs=Math.max(minMs,maxMs); }
    @Override public void apply(ChaosContext ctx) throws Exception {
        long d = ThreadLocalRandom.current().nextLong(minMs, maxMs+1);
        Thread.sleep(d);
    }
    @Override public String name(){ return "latency"; }
}