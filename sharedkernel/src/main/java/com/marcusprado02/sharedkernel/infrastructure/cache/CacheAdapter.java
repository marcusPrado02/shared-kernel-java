package com.marcusprado02.sharedkernel.infrastructure.cache;

import java.util.Optional;
import java.util.function.Function;

import reactor.core.publisher.Mono;

public interface CacheAdapter<V> {
  Mono<Optional<V>> get(String key, CacheOptions opts);
  Mono<Void> put(String key, V value, CacheOptions opts);
  Mono<Void> invalidate(String key);
  Mono<Void> invalidateByPrefix(String prefix);    // quando suportado (Redis SCAN)
  Mono<Long> size();                               // melhor-effort
  Mono<Void> clear();

  // “cache-aside”: carrega e popula sob demanda (com single-flight)
  default Mono<V> getOrLoad(String key, CacheOptions opts, Function<String, Mono<V>> loader) {
    return get(key, opts).flatMap(opt -> opt.map(Mono::just).orElseGet(() ->
      SingleFlight.execute(key, () ->
        loader.apply(key)
          .flatMap(v -> put(key, v, opts).onErrorResume(e -> Mono.empty()).thenReturn(v))
      )));
  }

  // Versões bloqueantes para uso não-reativo
  default Optional<V> getBlocking(String key, CacheOptions opts){ return get(key, opts).block(); }
  default void putBlocking(String key, V value, CacheOptions opts){ put(key, value, opts).block(); }
  default void invalidateBlocking(String key){ invalidate(key).block(); }
}