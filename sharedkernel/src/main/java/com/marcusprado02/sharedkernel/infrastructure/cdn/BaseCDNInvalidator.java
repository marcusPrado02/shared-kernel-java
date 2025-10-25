package com.marcusprado02.sharedkernel.infrastructure.cdn;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public abstract class BaseCDNInvalidator implements CDNInvalidator {

    protected final Tracer tracer;
    protected final MeterRegistry meter;
    protected final Retry retry;
    protected final CircuitBreaker cb;

    protected BaseCDNInvalidator(Tracer tracer, MeterRegistry meter, Retry retry, CircuitBreaker cb) {
        this.tracer = tracer; this.meter = meter; this.retry = retry; this.cb = cb;
    }

    @Override
    public InvalidateResponse invalidate(InvalidateRequest req) {
        var span = tracer.spanBuilder("cdn.invalidate")
                .setAttribute("cdn.backend", backendName())
                .setAttribute("cdn.distribution", req.distributionId())
                .setAttribute("cdn.targets.count", req.targets().size())
                .setAttribute("cdn.mode", req.mode().name())
                .startSpan();
        long start = System.nanoTime();

        try (var ignored = span.makeCurrent()) {
            InvalidateResponse resp = CircuitBreaker.decorateSupplier(cb,
                Retry.decorateSupplier(retry, () -> doInvalidate(req))
            ).get();

            meter.counter("cdn.invalidate.count", "backend", backendName()).increment();
            meter.timer("cdn.invalidate.latency", "backend", backendName())
                 .record(Duration.ofNanos(System.nanoTime() - start));
            return resp;
        } catch (Exception e) {
            span.recordException(e); span.setStatus(StatusCode.ERROR, e.getMessage());
            meter.counter("cdn.invalidate.errors", "backend", backendName()).increment();
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public CompletableFuture<InvalidateResponse> invalidateAsync(InvalidateRequest req) {
        return CompletableFuture.supplyAsync(() -> invalidate(req));
    }

    protected abstract InvalidateResponse doInvalidate(InvalidateRequest req);

    @Override
    public String backendName() { return getClass().getSimpleName(); }
}
