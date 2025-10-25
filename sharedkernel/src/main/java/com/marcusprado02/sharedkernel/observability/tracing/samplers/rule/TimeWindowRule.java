package com.marcusprado02.sharedkernel.observability.tracing.samplers.rule;


import java.time.LocalTime;

import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventAttributes;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventSampler;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.SampleDecision;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.SampleReason;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.util.DeterministicHash;

public final class TimeWindowRule implements SamplerRule {
    private final LocalTime start, end; private final double percentDuring;
    public TimeWindowRule(LocalTime start, LocalTime end, double percentDuring){
        this.start=start; this.end=end; this.percentDuring = Math.max(0, Math.min(1, percentDuring));
    }
    @Override public EventSampler.Result match(EventAttributes e) {
        java.time.LocalTime now = java.time.LocalTime.now();
        boolean within = start.isBefore(end) ? (now.isAfter(start) && now.isBefore(end))
                                             : (now.isAfter(start) || now.isBefore(end));
        if (within) {
            double h = DeterministicHash.hashToUnit(e.key!=null? e.key : e.name);
            return h < percentDuring ? new EventSampler.Result(SampleDecision.KEEP, SampleReason.TIME_WINDOW, percentDuring, 0) : null;
        }
        return null;
    }
}
