package com.marcusprado02.sharedkernel.infrastructure.messaging;

import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Consumidor genérico. */
public interface MessageConsumer {
  <T> Flux<MessageEnvelope<T>> subscribe(
      String topic,
      Class<T> payloadType,
      ConsumerOptions options,
      Function<HandlerInput<T>, Mono<Void>> handler // callback
  );

  /** Handler recebe envelope + contexto (para ack/nack granular). */
  record HandlerInput<T>(MessageEnvelope<T> message, MessageProcessingContext ctx) {}
}