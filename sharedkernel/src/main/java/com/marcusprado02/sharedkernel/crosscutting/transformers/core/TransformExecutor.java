package com.marcusprado02.sharedkernel.crosscutting.transformers.core;

import io.opentelemetry.api.trace.Tracer;

import com.marcusprado02.sharedkernel.crosscutting.idempotency.IdempotencyStore;

import io.opentelemetry.api.metrics.Meter;

public final class TransformExecutor {
    private final Tracer tracer;
    private final Meter meter;
    private final IdempotencyStore idStore; // opcional

    public TransformExecutor(Tracer tracer, Meter meter, IdempotencyStore idStore) {
        this.tracer = tracer; this.meter = meter; this.idStore = idStore;
    }

    public <I,O> TransformResult<O> execute(TransformFunction<I,O> fn, I in, TransformContext ctx, String idempotencyKey) {
        var span = tracer.spanBuilder("transform").startSpan();
        long t0 = System.nanoTime();
        try (var scope = span.makeCurrent()) {
            if (idStore != null && idempotencyKey != null) {
                var hit = idStore.tryGet(idempotencyKey);
                if (hit.isPresent()) return (TransformResult<O>) hit.get();
            }
            var r = fn.apply(in, ctx);
            meter.counterBuilder("transform.total").build().add(1, attrs(r));
            long ms = (System.nanoTime()-t0)/1_000_000; meter.histogramBuilder("transform.duration.ms").ofLongs().build().record(ms, attrs(r));
            span.setAttribute("outcome", r.outcome().name());
            if (idStore != null && idempotencyKey != null && r.outcome()==Outcome.OK) idStore.put(idempotencyKey, r, 600);
            return r;
        } catch (Throwable t) {
            span.recordException(t);
            meter.counterBuilder("transform.errors").build().add(1);
            return TransformResult.retry("exception:"+t.getClass().getSimpleName(), 200);
        } finally { span.end(); }
    }

    private io.opentelemetry.api.common.Attributes attrs(TransformResult<?> r) {
        return io.opentelemetry.api.common.Attributes.builder().put("outcome", r.outcome().name()).build();
    }
}

