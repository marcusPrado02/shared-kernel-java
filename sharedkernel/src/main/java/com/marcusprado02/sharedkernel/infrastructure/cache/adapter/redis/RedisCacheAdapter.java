package com.marcusprado02.sharedkernel.infrastructure.cache.adapter.redis;

import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;

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

public class RedisCacheAdapter<V> extends AbstractCacheAdapter<V> {

  private final RedissonClient redisson;

  public RedisCacheAdapter(RedissonClient client,
                           CacheSerializer<V> ser,
                           CacheKeyStrategy ks,
                           MeterRegistry m,
                           Tracer t,
                           CircuitBreaker cb,
                           Retry r,
                           Class<V> vt) {
    super(ser, ks, m, t, cb, r, vt);
    this.redisson = client;
  }

  @Override
  protected Mono<Optional<V>> doGet(String key, CacheOptions opts) {
    return Mono.fromCallable(() -> {
      RBucket<byte[]> bucket = redisson.getBucket(key);
      byte[] data = bucket.get();
      if (data == null) return Optional.empty();
      // Ajuste este método conforme a sua interface CacheSerializer
      return Optional.of(serializer.deserialize(data, opts.compress(), valueType));
    });
  }

  @Override
  protected Mono<Void> doPut(String key, V value, CacheOptions opts, Duration ttl) {
    return Mono.fromRunnable(() -> {
      byte[] bytes = serializer.serialize(value, opts.compress());
      redisson.getBucket(key).set(bytes, ttl);
    });
  }

  @Override
  public Mono<Void> invalidate(String key) {
    return Mono.fromRunnable(() -> redisson.getBucket(key).delete());
  }

  @Override
  public Mono<Void> invalidateByPrefix(String prefix) {
    return Mono.fromRunnable(() -> {
      RKeys keys = redisson.getKeys();
      Iterable<String> it = keys.getKeysByPattern(prefix + "*");
      for (String k : it) {
        redisson.getBucket(k).delete();
      }
    });
  }

  @Override
  public Mono<Long> size() {
    return Mono.fromCallable(() -> redisson.getKeys().count());
  }

  @Override
  public Mono<Void> clear() {
    return Mono.fromRunnable(() -> redisson.getKeys().flushdb());
  }

  public Mono<Long> publish(String channel, String message) {
    return Mono.fromCallable(() -> redisson.getTopic(channel).publish(message));
  }
}
