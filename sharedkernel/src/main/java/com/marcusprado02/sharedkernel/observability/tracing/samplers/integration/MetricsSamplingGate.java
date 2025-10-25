package com.marcusprado02.sharedkernel.observability.tracing.samplers.integration;


import java.time.Instant;
import java.util.Map;

import com.marcusprado02.sharedkernel.observability.metrics.core.MetricId;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventAttributes;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventKind;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventSampler;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.SampleDecision;

public final class MetricsSamplingGate {
    private final EventSampler sampler;
    public MetricsSamplingGate(EventSampler sampler){ this.sampler = sampler; }

    public boolean accept(MetricId id, Map<String,String> tags){
        var ev = new EventAttributes(
            EventKind.METRIC, id.fqName(), Instant.now(), null, (Map) tags, tags==null? null : tags.get("route")
        );
        var res = sampler.shouldSample(ev);
        return res.decision != SampleDecision.DROP;
    }
}