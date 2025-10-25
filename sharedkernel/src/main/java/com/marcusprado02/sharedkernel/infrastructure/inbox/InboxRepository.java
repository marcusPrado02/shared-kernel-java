package com.marcusprado02.sharedkernel.infrastructure.inbox;

import java.util.Optional;

import reactor.core.publisher.Mono;

public interface InboxRepository {
  /** Insere RECEIVED se não existir; retorna existente se já processado/falhou. */
  Mono<InboxRecord> putIfAbsent(InboxRecord record);

  /** Marca PROCESSING (com verificação de corrida). */
  Mono<Boolean> tryMarkProcessing(String messageId);

  Mono<Void> markProcessed(String messageId);
  Mono<Void> markFailed(String messageId, String error, int maxAttempts, java.time.Duration baseBackoff);
  Mono<Void> markDead(String messageId, String error);
  Mono<Optional<InboxRecord>> find(String messageId);
}
