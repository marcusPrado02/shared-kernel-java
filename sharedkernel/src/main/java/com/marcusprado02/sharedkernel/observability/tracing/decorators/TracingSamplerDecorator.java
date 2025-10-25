package com.marcusprado02.sharedkernel.observability.tracing.decorators;


import java.time.Instant;
import java.util.Map;

import com.marcusprado02.sharedkernel.observability.tracing.SpanConfig;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventAttributes;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventKind;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventSampler;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.SampleDecision;

public final class TracingSamplerDecorator implements TracingSpanDecorator {
    private final EventSampler sampler;
    public TracingSamplerDecorator(EventSampler sampler){ this.sampler = sampler; }

    @Override public SpanConfig beforeStart(SpanConfig cfg) {
        EventAttributes ev = new EventAttributes(
                EventKind.TRACE, cfg.name, Instant.now(), null, cfg.attributes, (String) cfg.attributes.getOrDefault("http.route", cfg.name)
        );
        var res = sampler.shouldSample(ev);
        if (res.decision == SampleDecision.DROP) {
            // Sinalize “amostra fraca” no início; o adapter pode marcar atributo sample.rate ou is_sampled=false
            return SpanConfig.builder(cfg.name).kind(cfg.kind).attrs(cfg.attributes).parent(cfg.parentSpanId).build();
        }
        if (res.decision == SampleDecision.DEFER) {
            // dica para collector tail-based (ex: OTEL 'sampled' pode ser decidido no collector via atributo)
            var b = SpanConfig.builder(cfg.name).kind(cfg.kind).attrs(cfg.attributes).parent(cfg.parentSpanId);
            b.attr("sampling.tail_ttl_sec", res.ttlSeconds);
            return b.build();
        }
        return cfg; // KEEP
    }
}
