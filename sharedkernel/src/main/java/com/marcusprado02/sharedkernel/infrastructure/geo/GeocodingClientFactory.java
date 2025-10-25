package com.marcusprado02.sharedkernel.infrastructure.geo;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodingClient;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.Place;
import com.marcusprado02.sharedkernel.infrastructure.geo.core.SmartGeocodingClient;
import com.marcusprado02.sharedkernel.infrastructure.geo.decorators.CachedGeocodingClient;
import com.marcusprado02.sharedkernel.infrastructure.geo.decorators.ResilientGeocodingClient;
import com.marcusprado02.sharedkernel.infrastructure.geo.decorators.TelemetryGeocodingClient;
import com.marcusprado02.sharedkernel.infrastructure.geo.spi.GeocodingProvider;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class GeocodingClientFactory {

    public static GeocodingClient defaultClient(
        OkHttpClient http, MeterRegistry metrics, Tracer tracer, Map<String,String> env
    ) {
        // Carrega provedores via ServiceLoader e injeta dependências mínimas com chaves
        List<GeocodingProvider> providers = ProviderBootstrap.load(http, metrics, env);

        var hedgedPool = Executors.newFixedThreadPool(Math.max(4, providers.size() * 2));
        GeocodingClient base = new SmartGeocodingClient(providers, hedgedPool, 2);

        // ---- Resilience (config sensata + parametrizada por env) ----
        Retry retry = Retry.of("geocoding", RetryConfig.custom()
            .maxAttempts(Integer.parseInt(env.getOrDefault("GEOCODING_RETRY_ATTEMPTS", "4")))
            .waitDuration(Duration.ofMillis(Long.parseLong(env.getOrDefault("GEOCODING_RETRY_BACKOFF_MS","200"))))
            .build());

        CircuitBreaker cb = CircuitBreaker.of("geocoding", CircuitBreakerConfig.ofDefaults());

        RateLimiter rl = RateLimiter.of("geocoding", RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .limitForPeriod(Integer.parseInt(env.getOrDefault("GEOCODING_RPS","20")))
            .timeoutDuration(Duration.ofMillis(Long.parseLong(env.getOrDefault("GEOCODING_RL_TIMEOUT_MS","200"))))
            .build());

        Bulkhead bh = Bulkhead.of("geocoding", BulkheadConfig.custom()
            .maxConcurrentCalls(Integer.parseInt(env.getOrDefault("GEOCODING_CONCURRENCY","50")))
            .maxWaitDuration(Duration.ofMillis(Long.parseLong(env.getOrDefault("GEOCODING_BH_WAIT_MS","100"))))
            .build());

        // Construtor existe exatamente com estes tipos:
        GeocodingClient resilient = new ResilientGeocodingClient(base, retry, cb, rl, bh, Executors.newScheduledThreadPool(2));

        // ---- Cache tipado (sem raw types) ----
        Cache<String, List<Place>> cache = Caffeine.newBuilder()
            .maximumSize(Long.parseLong(env.getOrDefault("GEOCODING_CACHE_SIZE","20000")))
            .expireAfterWrite(Duration.ofSeconds(Long.parseLong(env.getOrDefault("GEOCODING_TTL_SEC","600"))))
            .build();

        GeocodingClient cached = new CachedGeocodingClient(resilient, cache);
        return new TelemetryGeocodingClient(cached, metrics, tracer);
    }
}
