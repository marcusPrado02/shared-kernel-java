package com.marcusprado02.sharedkernel.infrastructure.outbox;

import java.time.Instant;
import java.util.Map;

/** Registro imutável do outbox. */
public record OutboxRecord(
    String id,                 // ULID/UUID
    String topic,              // destino lógico (ex: billing.payments.authorized)
    String key,                // chave/ordering (ex: aggregateId)
    String payload,            // JSON (CloudEvents / Avro-JSON) -> binário opcional
    String contentType,        // ex: application/cloudevents+json
    Map<String,String> headers,// correlation, causation, tenant, etc.
    Instant createdAt,
    OutboxStatus status,
    int attempt,
    Instant nextAttemptAt,
    String error,              // última falha resumida
    String shardKey,           // para sharding de workers (hash)
    String producerId,         // worker que CLAIMED
    Instant claimedAt
) {}
