package com.marcusprado02.sharedkernel.observability.tracing.samplers.rule;


import java.util.Map;

import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventAttributes;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventSampler;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.SampleDecision;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.SampleReason;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.util.DeterministicHash;

public final class PercentRule implements SamplerRule {
    private final double all;                   // p global 0..1
    private final Map<String, Double> bySev;    // INFO->0.1, ERROR->1.0 etc.

    public PercentRule(double all, Map<String,Double> bySev) {
        this.all = Math.max(0, Math.min(1, all));
        this.bySev = bySev==null? Map.of() : bySev;
    }

    @Override public EventSampler.Result match(EventAttributes e) {
        double p = bySev.getOrDefault(e.severity==null? "" : e.severity.toUpperCase(), all);
        // sem chave → usa hash do nome para estabilidade; com chave → usa key
        double h = DeterministicHash.hashToUnit(e.key!=null? e.key : e.name);
        return (h < p) ? new EventSampler.Result(SampleDecision.KEEP, SampleReason.PERCENT, p, 0) : null;
    }
}