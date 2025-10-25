package com.marcusprado02.sharedkernel.observability.chaos.conditions;

import com.marcusprado02.sharedkernel.observability.chaos.*;

public final class PercentCondition implements ChaosCondition {
    private final double p;
    public PercentCondition(double p){ this.p = Math.max(0, Math.min(1, p)); }
    @Override public double probability(ChaosContext ctx){ return p; }
    @Override public String describe(){ return "p=" + p; }
}