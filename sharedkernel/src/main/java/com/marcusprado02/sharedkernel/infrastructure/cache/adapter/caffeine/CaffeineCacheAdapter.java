package com.marcusprado02.sharedkernel.infrastructure.cache.adapter.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.marcusprado02.sharedkernel.infrastructure.cache.AbstractCacheAdapter;
import com.marcusprado02.sharedkernel.infrastructure.cache.CacheKeyStrategy;
import com.marcusprado02.sharedkernel.infrastructure.cache.CacheOptions;
import com.marcusprado02.sharedkernel.infrastructure.cache.CacheSerializer;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Adapter Caffeine simples (in-memory).
 * - TTL global pode ser definido no builder externo; aqui honramos TTL por entrada via invalidação programada.
 */
public class CaffeineCacheAdapter<V> extends AbstractCacheAdapter<V> {

  private static final ScheduledExecutorService TTL_EXEC =
      Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "caffeine-ttl");
        t.setDaemon(true);
        return t;
      });

  private final Cache<String, V> cache;

  public CaffeineCacheAdapter(CacheSerializer<V> serializer,
                              CacheKeyStrategy keyStrategy,
                              MeterRegistry metrics,
                              Tracer tracer,
                              CircuitBreaker cb,
                              Retry retry,
                              Class<V> valueType,
                              long maximumSize,
                              boolean recordStats) {
    super(serializer, keyStrategy, metrics, tracer, cb, retry, valueType);
    Caffeine<Object, Object> builder = Caffeine.newBuilder().maximumSize(maximumSize);
    if (recordStats) builder = builder.recordStats();
    // Observação: TTL global (expireAfterWrite) pode ser configurado aqui se sua política for fixa.
    this.cache = builder.build();
  }

  @Override
  protected Mono<Optional<V>> doGet(String key, CacheOptions opts) {
    return Mono.fromCallable(() -> Optional.ofNullable(cache.getIfPresent(key)));
  }

  @Override
  protected Mono<Void> doPut(String key, V value, CacheOptions opts, Duration ttl) {
    Objects.requireNonNull(key, "key");
    cache.put(key, value);

    // TTL por entrada (se informado): agenda uma invalidação.
    if (ttl != null && !ttl.isNegative() && !ttl.isZero()) {
      long delayMs = ttl.toMillis();
      TTL_EXEC.schedule(() -> cache.invalidate(key), delayMs, TimeUnit.MILLISECONDS);
    }
    return Mono.empty();
  }

  @Override
  public Mono<Void> invalidate(String key) {
    return Mono.fromRunnable(() -> cache.invalidate(key));
  }

  @Override
  public Mono<Void> invalidateByPrefix(String prefix) {
    return Mono.fromRunnable(() ->
        cache.asMap().keySet().stream()
            .filter(k -> k.startsWith(prefix))
            .forEach(cache::invalidate));
  }

  @Override
  public Mono<Long> size() {
    return Mono.fromCallable(() -> cache.estimatedSize());
  }

  @Override
  public Mono<Void> clear() {
    return Mono.fromRunnable(cache::invalidateAll);
  }
}
