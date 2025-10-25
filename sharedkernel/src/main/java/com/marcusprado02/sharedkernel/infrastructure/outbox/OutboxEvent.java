package com.marcusprado02.sharedkernel.infrastructure.outbox;

import java.time.Instant;

public record OutboxEvent(
    String id,
    String tenantId,
    String aggregateType,
    String aggregateId,
    String type,
    Instant occurredAt,
    String payload
) {}
