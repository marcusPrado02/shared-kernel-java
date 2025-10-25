package com.marcusprado02.sharedkernel.observability.chaos.conditions;

import com.marcusprado02.sharedkernel.observability.chaos.*;

public final class RouteMatchCondition implements ChaosCondition {
    private final String regex;
    public RouteMatchCondition(String regex){ this.regex = regex; }
    @Override public double probability(ChaosContext ctx){
        if (ctx.route == null) return 0;
        return ctx.route.matches(regex) ? 1.0 : 0.0;
    }
    @Override public String describe(){ return "route~=" + regex; }
}