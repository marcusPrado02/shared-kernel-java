package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Contrato supremo do EventStore. */
public interface EventStoreAdapter {

  // --- APPEND ---
  Mono<AppendResult> append(String streamId, List<EventEnvelope<? extends DomainEvent>> events, AppendOptions options);

  // Versão síncrona para camadas não reativas:
  default AppendResult appendBlocking(String streamId, List<EventEnvelope<? extends DomainEvent>> events, AppendOptions options) {
    return append(streamId, events, options).block();
  }

  // --- READ (por stream) ---
  Flux<EventEnvelope<?>> readStream(String streamId, ReadOptions options);

  // --- READ GLOBAL (para projeções/ETL) ---
  Flux<EventEnvelope<?>> readAll(GlobalPosition from, int batchSize);

  // --- SUBSCRIBE ---
  Flux<EventEnvelope<?>> subscribe(SubscriptionOptions options);

  // --- ADMIN ---
  Mono<Void> deleteStream(String streamId, boolean hardDelete);
  Mono<Void> truncateStream(String streamId, long truncateBeforeRevision);
  Mono<GlobalPosition> currentGlobalPosition();
}
