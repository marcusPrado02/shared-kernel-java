package com.marcusprado02.sharedkernel.infrastructure.messaging;


import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class AbstractMessageProducer implements MessageProducer {
  protected final MessageSerializer serializer;
  protected final MeterRegistry metrics;
  protected final Tracer tracer;
  protected final Retry retry;
  protected final CircuitBreaker cb;

  protected AbstractMessageProducer(MessageSerializer serializer, MeterRegistry metrics, Tracer tracer,
                                    Retry retry, CircuitBreaker cb) {
    this.serializer = serializer; this.metrics = metrics; this.tracer = tracer; this.retry = retry; this.cb = cb;
  }

  @Override
  public <T> Mono<Void> send(MessageEnvelope<T> message, ProducerOptions options) {
    var timer = io.micrometer.core.instrument.Timer.start(metrics);

    return Mono.defer(() -> doSend(message, options))
        .transformDeferred(RetryOperator.of(retry))           // operador Reactor p/ Retry (Mono)
        .transformDeferred(CircuitBreakerOperator.of(cb))         // operador Reactor p/ CircuitBreaker
        .doOnSuccess(v -> metrics.counter("msg.producer.sent", "topic", message.topic()).increment())
        .doOnError(e -> metrics.counter("msg.producer.errors", "topic", message.topic(),
                                        "type", e.getClass().getSimpleName()).increment())
        .doFinally(s -> timer.stop(metrics.timer("msg.producer.latency", "topic", message.topic())))
        .then();
  }

  @Override public <T> Mono<Void> sendBatch(Iterable<MessageEnvelope<T>> messages, ProducerOptions options) {
    return Flux.fromIterable(messages).flatMap(m -> send(m, options)).then();
  }

  protected abstract <T> Mono<Void> doSend(MessageEnvelope<T> message, ProducerOptions options);
}

