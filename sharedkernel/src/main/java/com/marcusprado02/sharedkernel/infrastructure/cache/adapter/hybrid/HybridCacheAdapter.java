package com.marcusprado02.sharedkernel.infrastructure.cache.adapter.hybrid;

import com.marcusprado02.sharedkernel.infrastructure.cache.AbstractCacheAdapter;
import com.marcusprado02.sharedkernel.infrastructure.cache.CacheKeyStrategy;
import com.marcusprado02.sharedkernel.infrastructure.cache.CacheOptions;
import com.marcusprado02.sharedkernel.infrastructure.cache.CacheSerializer;
import com.marcusprado02.sharedkernel.infrastructure.cache.adapter.caffeine.CaffeineCacheAdapter;
// Troque pelo seu pacote real do adapter Redis:
import com.marcusprado02.sharedkernel.infrastructure.cache.adapter.redis.RedisCacheAdapter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

public class HybridCacheAdapter<V> extends AbstractCacheAdapter<V> {

  private final CaffeineCacheAdapter<V> l1;
  private final RedisCacheAdapter<V> l2;
  private final String invalidateChannel; // Redis Pub/Sub

  public HybridCacheAdapter(CaffeineCacheAdapter<V> l1,
                            RedisCacheAdapter<V> l2,
                            CacheSerializer<V> serializer,
                            CacheKeyStrategy keyStrategy,
                            MeterRegistry metrics,
                            Tracer tracer,
                            CircuitBreaker cb,
                            Retry retry,
                            Class<V> valueType,
                            String invalidateChannel) {
    super(serializer, keyStrategy, metrics, tracer, cb, retry, valueType);
    this.l1 = l1;
    this.l2 = l2;
    this.invalidateChannel = invalidateChannel;
  }

  @Override
  protected Mono<Optional<V>> doGet(String key, CacheOptions o) {
    return l1.get(key, o).flatMap(optL1 -> {
      if (optL1.isPresent()) return Mono.just(optL1);
      return l2.get(key, o).flatMap(optL2 -> {
        optL2.ifPresent(v -> l1.put(key, v, o).subscribe());
        return Mono.just(optL2);
      });
    });
  }

  @Override
  protected Mono<Void> doPut(String key, V value, CacheOptions o, Duration ttl) {
    // persiste no L2 e popula o L1 em seguida
    return l2.put(key, value, o).then(l1.put(key, value, o));
  }

  @Override
  public Mono<Void> invalidate(String key) {
    return l2.invalidate(key)
        .then(l1.invalidate(key))
        .then(publishInvalidate(key));
  }

  private Mono<Void> publishInvalidate(String key) {
    if (invalidateChannel != null && !invalidateChannel.isBlank()) {
      return l2.publish(invalidateChannel, key).then();
    }
    return Mono.empty();
  }

  @Override public Mono<Void> invalidateByPrefix(String prefix) {
    return l2.invalidateByPrefix(prefix).then(l1.invalidateByPrefix(prefix));
  }

  @Override public Mono<Long> size() { return l2.size(); }

  @Override public Mono<Void> clear() { return l2.clear().then(l1.clear()); }
}
