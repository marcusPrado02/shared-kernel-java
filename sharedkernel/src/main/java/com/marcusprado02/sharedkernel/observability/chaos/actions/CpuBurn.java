package com.marcusprado02.sharedkernel.observability.chaos.actions;

import com.marcusprado02.sharedkernel.observability.chaos.*;

public final class CpuBurn implements ChaosAction {
    private final long millis;
    public CpuBurn(long millis){ this.millis = Math.max(10, Math.min(5_000, millis)); } // guardrail
    @Override public void apply(ChaosContext ctx){
        long end = System.currentTimeMillis() + millis;
        double x = 1; // operação boba
        while (System.currentTimeMillis() < end) { x = Math.sin(x) * Math.cos(x) + Math.sqrt(Math.abs(x)+1.0); }
    }
    @Override public String name(){ return "cpu"; }
}