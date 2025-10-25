package com.marcusprado02.sharedkernel.observability.tracing.decorators;

import java.util.*;
import java.util.regex.Pattern;

import com.marcusprado02.sharedkernel.observability.tracing.SpanConfig;
import com.marcusprado02.sharedkernel.observability.tracing.SpanHandle;

public final class PiiScrubberDecorator implements TracingSpanDecorator {
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+");
    private static final String REDACTED = "[REDACTED]";

    @Override public SpanConfig beforeStart(SpanConfig cfg) {
        Map<String,Object> out = new LinkedHashMap<>(cfg.attributes.size());
        cfg.attributes.forEach((k,v) -> out.put(k, scrub(v)));
        return SpanConfig.builder(cfg.name).kind(cfg.kind).attrs(out).parent(cfg.parentSpanId).build();
    }
    @Override public void onEvent(SpanHandle span, String name, Map<String,Object> attrs) {
        if (attrs == null) return;
        Map<String,Object> safe = new LinkedHashMap<>();
        attrs.forEach((k,v) -> safe.put(k, scrub(v)));
        span.addEvent(name, safe);
    }
    private Object scrub(Object v){
        if (v instanceof String s) {
            if (EMAIL.matcher(s).find()) return REDACTED;
            if (s.length() > 2048) return s.substring(0, 2048) + "...";
        }
        return v;
    }
}
