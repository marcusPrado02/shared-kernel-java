package com.marcusprado02.sharedkernel.observability.chaos.conditions;

import java.util.Set;

import com.marcusprado02.sharedkernel.observability.chaos.*;

public final class TenantAllowlistCondition implements ChaosCondition {
    private final Set<String> tenants;
    public TenantAllowlistCondition(Set<String> tenants){ this.tenants = tenants; }
    @Override public double probability(ChaosContext ctx){ return (ctx.tenant!=null && tenants.contains(ctx.tenant)) ? 1.0 : 0.0; }
    @Override public String describe(){ return "tenants="+tenants; }
}
