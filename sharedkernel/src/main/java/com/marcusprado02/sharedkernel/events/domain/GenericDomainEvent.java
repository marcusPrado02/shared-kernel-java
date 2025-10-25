package com.marcusprado02.sharedkernel.events.domain;

import java.time.Instant;
import java.util.Map;

/** Evento de domínio genérico para publicação no Outbox. */
public record GenericDomainEvent(
        String type,
        String aggregateId,
        Object payload,
        Instant occurredAt,
        Map<String, Object> metadata
) implements DomainEvent {

    @Override
    public String eventName() {
        return type;
    }

    @Override
    public String aggregateType() {
        return type;
    }

    @Override
    public String correlationId() {
        return metadata != null ? (String) metadata.get("correlationId") : null;
    }

    @Override
    public Instant occurredOn() {
        return occurredAt;
    }

    @Override
    public Object data() {
        return payload;
    }

    @Override
    public String id() {
        return metadata != null ? (String) metadata.get("id") : null;
    }

    @Override
    public String source() {
        return metadata != null ? (String) metadata.get("source") : null;
    }
}
