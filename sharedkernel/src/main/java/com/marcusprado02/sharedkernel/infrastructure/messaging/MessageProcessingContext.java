package com.marcusprado02.sharedkernel.infrastructure.messaging;

import reactor.core.publisher.Mono;

/** Contexto de processamento (ack/nack, commit offset). */
public interface MessageProcessingContext {
  Mono<Void> ack();              // confirma processamento
  Mono<Void> nack(Throwable t);  // rejeita e aplica política retry/DLQ
  String messageId();
  String topic();
  String key();
}