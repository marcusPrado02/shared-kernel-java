package com.marcusprado02.sharedkernel.observability.tracing.samplers.integration;


import java.time.Instant;
import java.util.Map;

import com.marcusprado02.sharedkernel.observability.logging.Severity;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventAttributes;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventKind;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventSampler;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.SampleDecision;

public final class SamplingFilter {
    private final EventSampler sampler;
    public SamplingFilter(EventSampler sampler){ this.sampler = sampler; }

    public boolean accept(Severity level, String loggerName, String message, Map<String,Object> fields) {
        EventAttributes ev = new EventAttributes(
                EventKind.LOG, loggerName, Instant.now(), level.name(), fields, (String) (fields==null? null : fields.get("route"))
        );
        var res = sampler.shouldSample(ev);
        // Opcional: adicionar hint de tail sampling ao campo (collector pode ler)
        if (res.decision == SampleDecision.DEFER && fields != null) {
            fields.put("sampling.tail_ttl_sec", res.ttlSeconds);
        }
        return res.decision != SampleDecision.DROP;
    }
}
