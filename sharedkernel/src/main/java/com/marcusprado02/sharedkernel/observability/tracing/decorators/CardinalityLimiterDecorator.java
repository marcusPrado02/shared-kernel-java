package com.marcusprado02.sharedkernel.observability.tracing.decorators;

import java.util.*;

import com.marcusprado02.sharedkernel.observability.tracing.*;

public final class CardinalityLimiterDecorator implements TracingSpanDecorator {
    private final int maxAttrs;
    private final int maxValueLen;
    private final Set<String> whitelistKeys;
    private final Set<String> dropKeys;

    public CardinalityLimiterDecorator(int maxAttrs, int maxValueLen, Set<String> whitelistKeys, Set<String> dropKeys) {
        this.maxAttrs = maxAttrs;
        this.maxValueLen = maxValueLen;
        this.whitelistKeys = whitelistKeys==null? Set.of(): Set.copyOf(whitelistKeys);
        this.dropKeys = dropKeys==null? Set.of(): Set.copyOf(dropKeys);
    }

    @Override public SpanConfig beforeStart(SpanConfig cfg) {
        Map<String,Object> out = new LinkedHashMap<>();
        for (var e : cfg.attributes.entrySet()) {
            var k = e.getKey();
            if (dropKeys.contains(k)) continue;
            if (!whitelistKeys.isEmpty() && !whitelistKeys.contains(k)) continue;
            Object v = e.getValue();
            if (v instanceof String s && s.length() > maxValueLen) v = s.substring(0, maxValueLen);
            out.put(k, v);
            if (out.size() >= maxAttrs) break;
        }
        return SpanConfig.builder(cfg.name).kind(cfg.kind).attrs(out).parent(cfg.parentSpanId).build();
    }
}