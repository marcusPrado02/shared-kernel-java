package com.marcusprado02.sharedkernel.domain.model.base;


import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Contrato mínimo para eventos de domínio. */
public interface DomainEvent {
    UUID eventId();
    Instant occurredOn();
    String eventType();                      // nome estável (para outbox/telemetria)
    Optional<Identifier> aggregateId();      // correlação
    default int schemaVersion() { return 1; } // evolução de payload
}