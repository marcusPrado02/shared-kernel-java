package com.marcusprado02.sharedkernel.observability.tracing.decorators;

import java.util.*;

import com.marcusprado02.sharedkernel.observability.tracing.*;

public final class CompositeSpanDecorator implements TracingSpanDecorator {
    private final List<TracingSpanDecorator> delegates;
    public CompositeSpanDecorator(List<TracingSpanDecorator> ds){ this.delegates = List.copyOf(ds); }

    @Override public SpanConfig beforeStart(SpanConfig cfg){
        SpanConfig cur = cfg;
        for (var d : delegates) try { cur = d.beforeStart(cur); } catch (Throwable ignore) {}
        return cur;
    }
    @Override public void afterStart(SpanHandle span, SpanConfig cfg){ for (var d : delegates) safe(() -> d.afterStart(span, cfg)); }
    @Override public void onError(SpanHandle span, Throwable error){ for (var d : delegates) safe(() -> d.onError(span, error)); }
    @Override public void beforeEnd(SpanHandle span){ for (var d : delegates) safe(() -> d.beforeEnd(span)); }
    @Override public void afterEnd(String traceId, String spanId, SpanConfig cfg){ for (var d : delegates) safe(() -> d.afterEnd(traceId, spanId, cfg)); }
    @Override public void onEvent(SpanHandle span, String name, Map<String,Object> attrs){ for (var d : delegates) safe(() -> d.onEvent(span, name, attrs)); }

    private void safe(Runnable r){ try { r.run(); } catch (Throwable ignore) {} }
}
