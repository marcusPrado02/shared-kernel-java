package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import io.github.resilience4j.retry.Retry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Adapter fino para aplicar Retry do Resilience4j em Flux/Mono via transformDeferred.
 */
final class RetryOperator {

  private RetryOperator() {}

  /** Use em Flux: flux.transformDeferred(RetryOperator.of(retry)) */
  @SuppressWarnings("unchecked")
  public static <T> Function<Flux<T>, Flux<T>> of(Retry retry) {
    return (Function<Flux<T>, Flux<T>>) (Object) io.github.resilience4j.reactor.retry.RetryOperator.of(retry);
  }
  /** Use em Mono: mono.transformDeferred(RetryOperator.ofMono(retry)) */
  @SuppressWarnings("unchecked")
  public static <T> Function<Mono<T>, Mono<T>> ofMono(Retry retry) {
    return (Function<Mono<T>, Mono<T>>) (Object) io.github.resilience4j.reactor.retry.RetryOperator.of(retry);
  }
  
}