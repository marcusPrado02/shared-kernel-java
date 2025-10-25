package com.marcusprado02.sharedkernel.infrastructure.cache;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;

public abstract class AbstractCacheAdapter<V> implements CacheAdapter<V> {
  protected final CacheSerializer<V> serializer;
  protected final CacheKeyStrategy keyStrategy;
  protected final MeterRegistry metrics;
  protected final Tracer tracer;
  protected final CircuitBreaker cb;
  protected final Retry retry;
  protected final Class<V> valueType;
  protected final Random rnd = new Random();

  protected AbstractCacheAdapter(CacheSerializer<V> ser, CacheKeyStrategy ks, MeterRegistry m,
                                 Tracer t, CircuitBreaker cb, Retry r, Class<V> vt) {
    this.serializer = ser; this.keyStrategy = ks; this.metrics = m; this.tracer = t; this.cb = cb; this.retry = r; this.valueType = vt;
  }

  protected Duration withJitter(CacheOptions o) {
    if (o.jitterFactor() <= 0) return o.ttl();
    long base = o.ttl().toMillis();
    long jitter = (long) (base * (rnd.nextDouble() * o.jitterFactor()));
    return Duration.ofMillis(base - jitter);
  }

  @Override public Mono<Optional<V>> get(String key, CacheOptions opts) {
    var timer = io.micrometer.core.instrument.Timer.start(metrics);
        return Mono.defer(() -> doGet(key, opts))
        .transform(RetryOperator.of(retry))
        .transform(CircuitBreakerOperator.of(cb))
        .doOnSuccess(v -> metrics.counter("cache.get", "hit", String.valueOf(v.isPresent())).increment())
        .doOnError(e -> metrics.counter("cache.get.errors", "type", e.getClass().getSimpleName()).increment())
        .doFinally(s -> timer.stop(metrics.timer("cache.get.latency")));
  }

  @Override public Mono<Void> put(String key, V value, CacheOptions opts) {
    if (!admit(key, value)) return Mono.empty();
    var ttl = withJitter(opts);
    return Mono.defer(() -> doPut(key, value, opts, ttl))
        .transform(RetryOperator.of(retry))
        .transform(CircuitBreakerOperator.of(cb))
        .doOnSuccess(v -> metrics.counter("cache.put").increment())
        .doOnError(e -> metrics.counter("cache.put.errors", "type", e.getClass().getSimpleName()).increment())
        .then();
  }

  protected boolean admit(String key, V value) { return true; }

  protected abstract Mono<Optional<V>> doGet(String key, CacheOptions opts);
  protected abstract Mono<Void> doPut(String key, V value, CacheOptions opts, Duration ttl);
  public abstract Mono<Void> invalidate(String key);
  public abstract Mono<Void> invalidateByPrefix(String prefix);
  public abstract Mono<Long> size();
  public abstract Mono<Void> clear();
}

