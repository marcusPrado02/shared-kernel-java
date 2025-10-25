package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public abstract class AbstractEventStoreAdapter implements EventStoreAdapter {
  protected final EventSerializer serializer;
  protected final List<EventUpcaster> upcasters;
  protected final IdempotencyStore idempotency;
  protected final OutboxPublisher outbox;
  protected final MeterRegistry metrics;
  protected final Tracer tracer;
  protected final CircuitBreaker cbAppend, cbRead;
  protected final Retry retryAppend, retryRead;

  protected AbstractEventStoreAdapter(
      EventSerializer serializer,
      List<EventUpcaster> upcasters,
      IdempotencyStore idempotency,
      OutboxPublisher outbox,
      MeterRegistry metrics,
      Tracer tracer,
      CircuitBreaker cbAppend,
      Retry retryAppend,
      CircuitBreaker cbRead,
      Retry retryRead
  ) {
    this.serializer = serializer;
    this.upcasters = List.copyOf(upcasters);
    this.idempotency = idempotency;
    this.outbox = outbox;
    this.metrics = metrics;
    this.tracer = tracer;
    this.cbAppend = cbAppend;
    this.retryAppend = retryAppend;
    this.cbRead = cbRead;
    this.retryRead = retryRead;
  }
  
  @Override
  public Mono<AppendResult> append(String streamId, List<EventEnvelope<? extends DomainEvent>> events, AppendOptions options) {
    var timer = Timer.start(metrics);

    return Mono.defer(() -> {
        if (events.isEmpty()) {
          return Mono.just(new AppendResult(streamId, currentStreamRevision(streamId), 0));
        }

        // Idempotência (fail-fast)
        if (options.requireIdempotency()) {
          for (var ev : events) {
            if (ev.dedupKey() == null || ev.dedupKey().isBlank()) {
              return Mono.error(new IllegalArgumentException("dedupKey obrigatório quando requireIdempotency=true"));
            }
            if (idempotency.wasAlreadyProcessed(ev.dedupKey())) {
              return Mono.just(new AppendResult(streamId, currentStreamRevision(streamId), 0)); // no-op
            }
          }
        }

        // Delegar para implementação concreta com Resilience4j (CircuitBreaker + Retry) em Reactor
        return Mono.defer(() -> doAppend(streamId, events, options))
            .transformDeferred(RetryOperator.ofMono(retryAppend))
            .transformDeferred(CircuitBreakerOperator.of(cbAppend))
            .doOnSuccess(res -> {
              // Memoriza idempotência
              if (options.requireIdempotency()) {
                events.forEach(e -> idempotency.remember(e.dedupKey(), e.eventId(), e.occurredAt()));
              }
              // Outbox
              if (options.publishToOutbox() && outbox != null) {
                outbox.publish((List) events);
              }
            })
            .doOnSuccess(res ->
              metrics.counter("eventstore.append.count", "stream", streamId).increment(events.size())
            )
            .doOnError(ex ->
              metrics.counter("eventstore.append.errors", "stream", streamId, "type", ex.getClass().getSimpleName()).increment()
            );
      })
      .doFinally(s -> timer.stop(metrics.timer("eventstore.append.latency", "stream", streamId)));
  }

  @Override
  public Flux<EventEnvelope<?>> readStream(String streamId, ReadOptions options) {
    return Flux.defer(() -> doReadStream(streamId, options))
        .transformDeferred(RetryOperator.of(retryRead))              // Retry (Flux)
        .transformDeferred(CircuitBreakerOperator.of(cbRead))        // CircuitBreaker (Flux)
        .<EventEnvelope<?>>map(this::applyUpcasters)
        .doOnSubscribe(s -> metrics.counter("eventstore.read.requests", "stream", streamId).increment());
  }

  @Override
  public Flux<EventEnvelope<?>> readAll(GlobalPosition from, int batchSize) {
    return Flux.defer(() -> doReadAll(from, batchSize))
        .transformDeferred(RetryOperator.of(retryRead))
        .transformDeferred(CircuitBreakerOperator.of(cbRead))
        .<EventEnvelope<?>>map(this::applyUpcasters);
  }

  @Override
  public Flux<EventEnvelope<?>> subscribe(SubscriptionOptions options) {
    return Flux.defer(() -> doSubscribe(options))
        .transformDeferred(CircuitBreakerOperator.of(cbRead))
        .<EventEnvelope<?>>map(this::applyUpcasters)
        .doOnNext(ev -> Span.current().addEvent("event.received", io.opentelemetry.api.common.Attributes.empty()));
  }


  /** Implementações concretas devem armazenar e retornar o nextExpectedRevision. */
  protected abstract Mono<AppendResult> doAppend(String streamId, List<EventEnvelope<? extends DomainEvent>> events, AppendOptions options);
  protected abstract Flux<EventEnvelope<?>> doReadStream(String streamId, ReadOptions options);
  protected abstract Flux<EventEnvelope<?>> doReadAll(GlobalPosition from, int batchSize);
  protected abstract Flux<EventEnvelope<?>> doSubscribe(SubscriptionOptions options);
  protected abstract long currentStreamRevision(String streamId);

  private EventEnvelope<?> applyUpcasters(EventEnvelope<?> ev) {
    EventEnvelope<?> current = ev;
    for (var up : upcasters) {
      Optional<EventEnvelope<?>> upc = up.tryUpcast(current);
      if (upc.isPresent()) current = upc.get();
    }
    return current;
  }
}