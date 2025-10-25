package com.marcusprado02.sharedkernel.observability.tracing.adapters.otel;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

import java.util.*;

import com.marcusprado02.sharedkernel.observability.tracing.SpanConfig;
import com.marcusprado02.sharedkernel.observability.tracing.SpanHandle;
import com.marcusprado02.sharedkernel.observability.tracing.TracingFacade;
import com.marcusprado02.sharedkernel.observability.tracing.decorators.*;

public final class OpenTelemetryTracingAdapter implements TracingFacade {
    private final Tracer tracer;
    private final TracingSpanDecorator decorator;

    public OpenTelemetryTracingAdapter(String instrumentationName, TracingSpanDecorator decorator){
        this.tracer = GlobalOpenTelemetry.getTracer(instrumentationName==null? "app" : instrumentationName);
        this.decorator = decorator;
    }

    @Override public SpanHandle startSpan(SpanConfig cfg) {
        SpanConfig c = decorator==null? cfg : decorator.beforeStart(cfg);

        SpanKind otelKind = switch (c.kind) {
            case SERVER -> SpanKind.SERVER;
            case CLIENT -> SpanKind.CLIENT;
            case PRODUCER -> SpanKind.PRODUCER;
            case CONSUMER -> SpanKind.CONSUMER;
            default -> SpanKind.INTERNAL;
        };

        var builder = tracer.spanBuilder(c.name).setSpanKind(otelKind);

        if (c.parentSpanId != null) {
            // Best-effort: manter contexto atual; parentId específico exigiria propagadores custom
        }

        // atributos iniciais
        for (var e : c.attributes.entrySet()) {
            putAttr(builder, e.getKey(), e.getValue());
        }

        Span span = builder.startSpan();
        var scope = span.makeCurrent();

        var handle = new SpanHandle() {
            @Override public void setAttribute(String k, String v){ span.setAttribute(k, v); }
            @Override public void setAttribute(String k, long v){ span.setAttribute(k, v); }
            @Override public void setAttribute(String k, double v){ span.setAttribute(k, v); }
            @Override public void setAttribute(String k, boolean v){ span.setAttribute(k, v); }

            @Override public void addEvent(String name, Map<String,Object> attrs){
                if (attrs == null || attrs.isEmpty()) { span.addEvent(name); return; }
                AttributesBuilder ab = Attributes.builder();
                attrs.forEach((k,v)-> putAttr(ab, k, v));
                span.addEvent(name, ab.build());
            }

            @Override public void recordException(Throwable error, Map<String,Object> attrs){
                if (error == null) return;
                Attributes a = (attrs==null||attrs.isEmpty())? Attributes.empty() :
                        attrs.entrySet().stream().collect(Attributes::builder, (b,e)->putAttr(b,e.getKey(),e.getValue()), (a1,a2)->{}).build();
                span.recordException(error, a);
            }

            @Override public void setStatus(com.marcusprado02.sharedkernel.observability.tracing.StatusCode code, String description){
                var ot = switch (code) {
                    case OK -> StatusCode.OK;
                    case ERROR -> StatusCode.ERROR;
                    default -> StatusCode.UNSET;
                };
                span.setStatus(ot, description==null? "" : description);
            }

            @Override public String spanId(){ return span.getSpanContext().getSpanId(); }
            @Override public String traceId(){ return span.getSpanContext().getTraceId(); }

            @Override public void close() {
                try {
                    if (decorator != null) decorator.beforeEnd(this);
                } catch (Throwable ignore){}
                try { span.end(); } finally { scope.close(); }
                try {
                    if (decorator != null) decorator.afterEnd(traceId(), spanId(), c);
                } catch (Throwable ignore){}
            }
        };

        try { if (decorator != null) decorator.afterStart(handle, c); } catch (Throwable ignore) {}
        return handle;
    }

    @Override public String backend(){ return "opentelemetry"; }

    /* helpers */
    private static void putAttr(SpanBuilder b, String k, Object v) {
        if (v instanceof String s) b.setAttribute(k, s);
        else if (v instanceof Long l) b.setAttribute(k, l);
        else if (v instanceof Integer i) b.setAttribute(k, i.longValue());
        else if (v instanceof Double d) b.setAttribute(k, d);
        else if (v instanceof Float f) b.setAttribute(k, (double) f);
        else if (v instanceof Boolean bo) b.setAttribute(k, bo);
        else if (v != null) b.setAttribute(k, String.valueOf(v));
    }
    private static void putAttr(AttributesBuilder b, String k, Object v) {
        if (v instanceof String s) b.put(AttributeKey.stringKey(k), s);
        else if (v instanceof Long l) b.put(AttributeKey.longKey(k), l);
        else if (v instanceof Integer i) b.put(AttributeKey.longKey(k), i.longValue());
        else if (v instanceof Double d) b.put(AttributeKey.doubleKey(k), d);
        else if (v instanceof Float f) b.put(AttributeKey.doubleKey(k), (double) f);
        else if (v instanceof Boolean bo) b.put(AttributeKey.booleanKey(k), bo);
        else if (v != null) b.put(AttributeKey.stringKey(k), String.valueOf(v));
    }
}
