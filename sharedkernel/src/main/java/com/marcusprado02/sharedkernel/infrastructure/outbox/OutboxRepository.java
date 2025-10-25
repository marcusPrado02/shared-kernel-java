package com.marcusprado02.sharedkernel.infrastructure.outbox;

import java.time.Duration;
import java.time.Instant;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutboxRepository {
  Mono<Void> insert(OutboxRecord rec);  // usado dentro da transação de negócio
  /** Claim por lease com particionamento (shard) e limite. */
  Flux<OutboxRecord> claimBatch(String shard, int limit, Duration lease, Instant now);
  Mono<Void> markSent(String id);
  Mono<Void> markFailed(String id, String error, int maxAttempts, Duration retryBackoff);
  Mono<Void> markDead(String id, String error);
  Mono<Void> release(String id); // solta o lease (rollback do worker)
}
