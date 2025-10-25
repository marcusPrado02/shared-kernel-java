package com.marcusprado02.sharedkernel.infrastructure.geo.decorators;

import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodeOptions;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodingClient;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.Place;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class ResilientGeocodingClient implements GeocodingClient {
    private final GeocodingClient delegate;
    private final Retry retry;
    private final CircuitBreaker cb;
    private final RateLimiter rl;
    private final Bulkhead bh;
    private final ScheduledExecutorService scheduler;

    public ResilientGeocodingClient(GeocodingClient delegate,
                                    Retry retry,
                                    CircuitBreaker cb,
                                    RateLimiter rl,
                                    Bulkhead bh,
                                    ScheduledExecutorService scheduler) {
        this.delegate = delegate;
        this.retry = retry;
        this.cb = cb;
        this.rl = rl;
        this.bh = bh;
        this.scheduler = scheduler;
    }

    @Override
    public CompletableFuture<List<Place>> geocode(String q, GeocodeOptions o) {
        Supplier<CompletableFuture<List<Place>>> sup = () -> delegate.geocode(q, o);
        return decorate(sup);
    }

    @Override
    public CompletableFuture<List<Place>> reverse(double lat, double lon, GeocodeOptions o) {
        Supplier<CompletableFuture<List<Place>>> sup = () -> delegate.reverse(lat, lon, o);
        return decorate(sup);
    }

    private <T> CompletableFuture<T> decorate(Supplier<CompletableFuture<T>> supplier) {
        // encadeia os decorators SEM usar io.github.resilience4j.decorators.Decorators
        Supplier<CompletionStage<T>> d =
            () -> supplier.get(); // Supplier<CompletionStage<T>>

        d = io.github.resilience4j.bulkhead.Bulkhead
                .decorateCompletionStage(bh, d);
        d = io.github.resilience4j.ratelimiter.RateLimiter
                .decorateCompletionStage(rl, d);
        d = io.github.resilience4j.circuitbreaker.CircuitBreaker
                .decorateCompletionStage(cb, d);
        d = io.github.resilience4j.retry.Retry
                .decorateCompletionStage(retry, scheduler, d);

        return d.get().toCompletableFuture();
    }
}
