package com.marcusprado02.sharedkernel.observability.chaos.conditions;

import java.time.*;

import com.marcusprado02.sharedkernel.observability.chaos.*;

public final class WindowCondition implements ChaosCondition {
    private final LocalTime start, end;
    public WindowCondition(LocalTime start, LocalTime end){ this.start=start; this.end=end; }
    @Override public double probability(ChaosContext ctx) {
        LocalTime now = LocalTime.now();
        boolean within = start.isBefore(end) ? (now.isAfter(start) && now.isBefore(end))
                                             : (now.isAfter(start) || now.isBefore(end));
        return within ? 1.0 : 0.0;
    }
    @Override public String describe(){ return "window="+start+".."+end; }
}