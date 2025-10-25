package com.marcusprado02.sharedkernel.infrastructure.messaging;


import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class AbstractMessageConsumer implements MessageConsumer {
  protected final MessageSerializer serializer;
  protected final MeterRegistry metrics;
  protected final Tracer tracer;

  protected AbstractMessageConsumer(MessageSerializer serializer, MeterRegistry metrics, Tracer tracer) {
    this.serializer = serializer; this.metrics = metrics; this.tracer = tracer;
  }

  @Override public <T> Flux<MessageEnvelope<T>> subscribe(String topic, Class<T> payloadType, ConsumerOptions options,
                                                          java.util.function.Function<HandlerInput<T>, Mono<Void>> handler) {
    return doSubscribe(topic, payloadType, options)
        .flatMap(env -> {
          metrics.counter("msg.consumer.received", "topic", topic).increment();
          return handler.apply(new HandlerInput<>(env, contextFor(env, options)))
              .thenReturn(env)
              .onErrorResume(ex -> handleFailure(env, ex, options).then(Mono.empty()));
        });
  }

  protected abstract <T> Flux<MessageEnvelope<T>> doSubscribe(String topic, Class<T> payloadType, ConsumerOptions options);
  protected abstract <T> MessageProcessingContext contextFor(MessageEnvelope<T> env, ConsumerOptions options);
  protected abstract <T> Mono<Void> handleFailure(MessageEnvelope<T> env, Throwable ex, ConsumerOptions options);
}