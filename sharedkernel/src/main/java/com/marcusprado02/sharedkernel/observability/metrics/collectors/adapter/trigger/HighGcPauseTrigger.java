package com.marcusprado02.sharedkernel.observability.metrics.collectors.adapter.trigger;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.marcusprado02.sharedkernel.observability.metrics.collectors.*;
import com.marcusprado02.sharedkernel.observability.profiling.*;

public final class HighGcPauseTrigger implements ProfilingTrigger, GcListener {
    private final double p99ThresholdMs;
    private final Duration stableFor;
    private final AtomicReference<Long> firstAbove = new AtomicReference<>(null);
    private volatile GcSnapshot last;

    public HighGcPauseTrigger(double p99ThresholdMs, Duration stableFor){
        this.p99ThresholdMs=p99ThresholdMs; this.stableFor=stableFor;
    }

    @Override public void onGc(GcEvent evt, GcSnapshot snap) { last = snap; }

    @Override public EvaluationResult evaluate(ProfilingContext ctx) {
        var snap = last;
        if (snap==null) return EvaluationResult.suppress("no_data");
        if (snap.pauseP99ms() >= p99ThresholdMs) {
            long now = System.currentTimeMillis();
            var first = firstAbove.updateAndGet(prev -> prev==null?now:prev);
            if (now - first >= stableFor.toMillis()) {
                firstAbove.set(null);
                return EvaluationResult.trigger("high_gc_pause_p99", Map.of("p99ms", snap.pauseP99ms(), "thr", p99ThresholdMs));
            }
            return EvaluationResult.suppress("debouncing_gc");
        } else {
            firstAbove.set(null);
            return EvaluationResult.suppress("gc_ok");
        }
    }
}
