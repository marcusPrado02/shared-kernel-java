package com.marcusprado02.sharedkernel.observability.profiling.triggers;

import com.marcusprado02.sharedkernel.observability.profiling.*;
import java.util.Collections;

public final class ManualFlagTrigger implements ProfilingTrigger {
    private volatile boolean armed = false;
    public void arm(){ this.armed = true; }
    @Override public EvaluationResult evaluate(ProfilingContext ctx) {
        if (armed) { armed = false; return EvaluationResult.trigger("manual_arm", Collections.emptyMap()); }
        return EvaluationResult.suppress("manual_idle");
    }
}
