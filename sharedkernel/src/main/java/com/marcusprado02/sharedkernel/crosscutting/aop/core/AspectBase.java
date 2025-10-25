package com.marcusprado02.sharedkernel.crosscutting.aop.core;


import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.MDC;

import java.time.Clock;
import java.time.Instant;

public abstract class AspectBase implements AroundAdvice {
    protected final Tracer tracer;
    protected final Meter meter;
    protected final Clock clock;
    private final boolean failOpen;

    protected AspectBase(Tracer tracer, Meter meter, Clock clock, boolean failOpen) {
        this.tracer = tracer;
        this.meter = meter;
        this.clock = clock;
        this.failOpen = failOpen;
    }

    @Override
    public Object invoke(Invocation inv) throws Throwable {
        Instant start = Instant.now(clock);
        Span span = tracer.spanBuilder(getSpanName(inv)).startSpan();
        try (var scope = span.makeCurrent()) {
            MDC.put("aop.class", inv.getClassName());
            MDC.put("aop.method", inv.getMethodName());
            before(inv, span);
            Object result = inv.proceed();
            after(inv, result, span, start);
            return result;
        } catch (Throwable t) {
            onError(inv, t, span, start);
            if (failOpen) return inv.proceed(); // opcional: tenta prosseguir
            throw t;
        } finally {
            MDC.remove("aop.class");
            MDC.remove("aop.method");
            span.end();
        }
    }

    protected String getSpanName(Invocation inv) {
        return "aop." + inv.getDeclaringClass().getSimpleName() + "." + inv.getMethodName();
    }

    protected void before(Invocation inv, Span span) {}
    protected void after(Invocation inv, Object result, Span span, Instant start) {
        var duration = Instant.now(clock).toEpochMilli() - start.toEpochMilli();
        meter.counterBuilder("aop.invocations").build().add(1, Attributes.builder()
            .put("class", inv.getClassName()).put("method", inv.getMethodName()).build());
        meter.histogramBuilder("aop.duration.ms").ofLongs().build().record(duration);
        span.setAttribute("aop.duration.ms", duration);
    }
    protected void onError(Invocation inv, Throwable t, Span span, Instant start) {
        span.recordException(t);
        meter.counterBuilder("aop.errors").build().add(1);
    }
}
