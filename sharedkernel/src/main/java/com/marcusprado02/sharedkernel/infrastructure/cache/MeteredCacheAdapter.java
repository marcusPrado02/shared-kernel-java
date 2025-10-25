package com.marcusprado02.sharedkernel.infrastructure.cache;

import java.util.Optional;

import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Mono;

public class MeteredCacheAdapter<V> implements CacheAdapter<V> {
  private final CacheAdapter<V> delegate;
  private final MeterRegistry metrics;
  private final String namespace;
  public MeteredCacheAdapter(CacheAdapter<V> d, MeterRegistry m, String ns){ this.delegate=d; this.metrics=m; this.namespace=ns; }

  @Override public Mono<Optional<V>> get(String key, CacheOptions o) {
    return delegate.get(key, o).doOnSuccess(opt ->
      metrics.counter("cache.get", "ns", namespace, "hit", String.valueOf(opt.isPresent())).increment());
  }
  @Override public Mono<Void> put(String key, V value, CacheOptions o) { metrics.counter("cache.put", "ns", namespace).increment(); return delegate.put(key, value, o); }
  @Override public Mono<Void> invalidate(String key) { return delegate.invalidate(key); }
  @Override public Mono<Void> invalidateByPrefix(String prefix) { return delegate.invalidateByPrefix(prefix); }
  @Override public Mono<Long> size() { return delegate.size(); }
  @Override public Mono<Void> clear() { return delegate.clear(); }
}

