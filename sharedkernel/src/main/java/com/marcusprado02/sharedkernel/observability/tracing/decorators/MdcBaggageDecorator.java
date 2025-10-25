package com.marcusprado02.sharedkernel.observability.tracing.decorators;

import org.slf4j.MDC;

import com.marcusprado02.sharedkernel.observability.tracing.*;

import java.util.*;

public final class MdcBaggageDecorator implements TracingSpanDecorator {
    private final Set<String> mdcKeys;
    private final Map<String,String> staticAttrs;

    public MdcBaggageDecorator(Set<String> mdcKeys, Map<String,String> staticAttrs){
        this.mdcKeys = mdcKeys==null? Set.of(): Set.copyOf(mdcKeys);
        this.staticAttrs = staticAttrs==null? Map.of(): Map.copyOf(staticAttrs);
    }

    @Override public SpanConfig beforeStart(SpanConfig cfg) {
        var b = SpanConfig.builder(cfg.name).kind(cfg.kind).attrs(cfg.attributes).parent(cfg.parentSpanId);
        // inclui MDC seguro como atributos iniciais
        for (var k : mdcKeys) {
            var v = MDC.get(k);
            if (v != null && !v.isBlank()) b.attr("ctx."+k, v);
        }
        for (var e : staticAttrs.entrySet()) b.attr(e.getKey(), e.getValue());
        return b.build();
    }
}

