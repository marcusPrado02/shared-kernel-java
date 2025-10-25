package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import java.time.Instant;

/** Loja de idempotência (dedupeKey -> eventId/estado). */
public interface IdempotencyStore {
  /** @return true se chave já foi vista/confirmada. */
  boolean wasAlreadyProcessed(String dedupKey);
  void remember(String dedupKey, String eventId, Instant at);
}
