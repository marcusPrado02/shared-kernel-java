package com.marcusprado02.sharedkernel.domain.events.model;

import java.time.Instant;
import java.util.Map;

public record EventMetadata(
        String eventId,
        EventType eventType,
        int eventVersion,         // versão do "schema" do evento
        String aggregateType,
        String aggregateId,
        long sequence,
        long aggregateVersion,
        String tenantId,
        String correlationId,
        String causationId,
        Instant occurredAt,
        Map<String, String> tags
) {}