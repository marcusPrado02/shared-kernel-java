package com.marcusprado02.sharedkernel.infrastructure.inbox;

import java.time.Instant;
import java.util.Map;

/** Registro do inbox (idempotência de entrada). */
public record InboxRecord(
    String messageId,
    String topic,
    String key,
    String payload,               // JSON (CloudEvents/Avro-JSON)
    String contentType,
    Map<String,String> headers,
    Instant receivedAt,
    InboxStatus status,
    int attempt,
    Instant nextAttemptAt,
    String error
) {}
