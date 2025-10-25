package com.marcusprado02.sharedkernel.infrastructure.search;

import io.github.resilience4j.retry.*;
import io.github.resilience4j.circuitbreaker.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public abstract class BaseSearchAdapter<T> implements SearchAdapter<T> {

    protected final Tracer tracer;
    protected final MeterRegistry meter;
    protected final Retry retry;
    protected final CircuitBreaker cb;
    protected final CacheAdapter cache;

    protected BaseSearchAdapter(Tracer tracer,
                                MeterRegistry meter,
                                Retry retry,
                                CircuitBreaker cb,
                                CacheAdapter cache) {
        this.tracer = tracer;
        this.meter = meter;
        this.retry = retry;
        this.cb = cb;
        this.cache = cache;
    }

    @Override
    public PageResult<T> search(SearchQuery query, ProjectionMapper<Map<String, Object>, T> mapper) {
        final String cacheKey = cacheKey(query);
        if (cache != null && isCacheable(query)) {
            var cached = cache.<T>get(cacheKey, null);
            if (cached.isPresent()) return cached.get();
        }

        var span = tracer.spanBuilder("search")
                .setAttribute("search.backend", backendName())
                .setAttribute("search.index", query.indexOrCollection())
                .setAttribute("search.timeout.ms", query.timeout().toMillis())
                .startSpan();

        try (var scope = span.makeCurrent()) {
            long start = System.nanoTime();

            PageResult<T> result = CircuitBreaker.decorateSupplier(cb,
                    Retry.decorateSupplier(retry,
                            () -> doSearch(query, mapper)
                    )
            ).get();

            meter.counter("search.requests", "backend", backendName()).increment();
            meter.timer("search.latency", "backend", backendName())
                 .record(Duration.ofNanos(System.nanoTime() - start));

            if (cache != null && isCacheable(query)) cache.put(cacheKey, result, cacheTtl(query));
            return result;

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            meter.counter("search.errors", "backend", backendName()).increment();
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public CompletableFuture<PageResult<T>> searchAsync(SearchQuery query, ProjectionMapper<Map<String, Object>, T> mapper) {
        return CompletableFuture.supplyAsync(() -> search(query, mapper));
    }

    protected boolean isCacheable(SearchQuery q) {
        return q.page().cursor() == null && q.highlight().isEmpty() && q.timeout().compareTo(Duration.ofSeconds(1)) >= 0;
    }

    protected Duration cacheTtl(SearchQuery q) { return Duration.ofSeconds(30); }

    protected String cacheKey(SearchQuery q) {
        return q.indexOrCollection() + "|" + q.page().page() + "|" + q.page().size() + "|" +
                q.fullText().orElse("") + "|" + q.hybrid().map(Object::toString).orElse("") + "|" +
                q.filters().hashCode() + "|" + q.sort().hashCode() + "|" + q.facets().hashCode() + "|" + q.aggregations().hashCode() +
                q.tenant().map(t -> "|" + t.tenantId() + "|" + t.routingKey()).orElse("");
    }

    // Backend específico implementa esta operação
    protected abstract PageResult<T> doSearch(SearchQuery query, ProjectionMapper<Map<String, Object>, T> mapper);
}
