package com.marcusprado02.sharedkernel.infrastructure.messaging;

import java.util.Optional;

/** Opções de produção. */
public record ProducerOptions(
    boolean requireIdempotency,     // exige messageId/dedup
    boolean transactional,          // usa transações do broker (se suportado)
    Optional<Integer> partition,    // força partição (quando aplicável)
    Optional<Integer> compressionLevel,
    boolean asyncAcks,              // confirmações assíncronas (publisher confirms)
    int timeoutMs
) {
  public static ProducerOptions defaults() {
    return new ProducerOptions(true, true, Optional.empty(), Optional.empty(), true, 5000);
  }
}

