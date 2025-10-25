package com.marcusprado02.sharedkernel.observability.chaos.actions;

import com.marcusprado02.sharedkernel.observability.chaos.*;

public final class Throttle implements ChaosAction {
    private final long perRequestMs;
    public Throttle(long perRequestMs){ this.perRequestMs = Math.max(0, perRequestMs); }
    @Override public void apply(ChaosContext ctx) throws Exception { Thread.sleep(perRequestMs); }
    @Override public String name(){ return "throttle"; }
}