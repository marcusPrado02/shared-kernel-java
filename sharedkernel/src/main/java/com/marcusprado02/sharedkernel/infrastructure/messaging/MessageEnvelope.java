package com.marcusprado02.sharedkernel.infrastructure.messaging;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Envelope imutável de mensagens (CloudEvents-friendly). */
public record MessageEnvelope<T>(
    String messageId,            // ULID/UUID
    String topic,                // ex.: "payments.authorized"
    String key,                  // partição/ordering (aggregateId)
    T payload,                   // DTO/Avro/JSON POJO
    MessageHeaders headers,      // correlation/causation/tenant/etc.
    Instant occurredAt
) {}
