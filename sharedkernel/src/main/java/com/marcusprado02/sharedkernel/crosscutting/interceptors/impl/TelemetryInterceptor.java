package com.marcusprado02.sharedkernel.crosscutting.interceptors.impl;

import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptionContext;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Interceptor;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptorChain;

public class TelemetryInterceptor<TCtx extends InterceptionContext> implements Interceptor<TCtx> {
    private final io.opentelemetry.api.trace.Tracer tracer;
    private final io.opentelemetry.api.metrics.Meter meter;

    public TelemetryInterceptor(io.opentelemetry.api.trace.Tracer tracer, io.opentelemetry.api.metrics.Meter meter) {
        this.tracer = tracer; this.meter = meter;
    }

    @Override public Object around(TCtx ctx, InterceptorChain<TCtx> chain) throws Exception {
        var span = tracer.spanBuilder("intercept:" + ctx.operation()).startSpan();
        long t0 = System.nanoTime();
        try (var scope = span.makeCurrent()) {
            ctx.attributes().forEach((k,v) -> span.setAttribute("ctx."+k, String.valueOf(v)));
            return chain.proceed(ctx);
        } catch (Exception e) {
            span.recordException(e); throw e;
        } finally {
            long durMs = (System.nanoTime() - t0) / 1_000_000;
            meter.histogramBuilder("interceptor.duration.ms").ofLongs().build().record(durMs);
            span.setAttribute("duration.ms", durMs); span.end();
        }
    }
}
