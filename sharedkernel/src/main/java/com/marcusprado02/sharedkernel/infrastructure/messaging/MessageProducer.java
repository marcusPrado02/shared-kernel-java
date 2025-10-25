package com.marcusprado02.sharedkernel.infrastructure.messaging;

import reactor.core.publisher.Mono;

/** Produtor genérico. */
public interface MessageProducer {
  <T> Mono<Void> send(MessageEnvelope<T> message, ProducerOptions options);
  <T> Mono<Void> sendBatch(Iterable<MessageEnvelope<T>> messages, ProducerOptions options);
}
