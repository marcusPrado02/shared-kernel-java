package com.marcusprado02.sharedkernel.infrastructure.inbox;

import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageEnvelope;

import reactor.core.publisher.Mono;

/** Orquestrador principal. */
public interface InboxProcessor {
  <T> Mono<Void> onMessage(
      MessageEnvelope<T> message,     // do seu módulo messaging
      Class<T> dtoType,
      ProcessingOptions options
  );
}
