package com.marcusprado02.sharedkernel.infrastructure.inbox;

import reactor.core.publisher.Mono;

/** Contrato do handler de aplicação (idempotente por mensagem). */
@FunctionalInterface
public interface MessageHandler<T> {
  /**
   * Processa a mensagem dentro de uma transação de aplicação.
   * Deve aplicar mudanças no domínio, append no EventStore e enfileirar Outbox.
   */
  Mono<Void> handle(ProcessingContext ctx, T dto);
}
