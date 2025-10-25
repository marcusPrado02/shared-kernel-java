package com.marcusprado02.sharedkernel.infrastructure.cache;

import java.util.concurrent.*;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;

final class SingleFlight {
  private static final ConcurrentHashMap<String, Mono<?>> inflight = new ConcurrentHashMap<>();
  @SuppressWarnings("unchecked")
  static <T> Mono<T> execute(String key, Supplier<Mono<T>> supplier) {
    return (Mono<T>) inflight.computeIfAbsent(key, k -> supplier.get().doFinally(sig -> inflight.remove(k)).cache());
  }
}
