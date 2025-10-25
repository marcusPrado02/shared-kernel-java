package com.marcusprado02.sharedkernel.domain.model.base;


import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public abstract class BaseDomainEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredOn = Instant.now();

    @Override public UUID eventId() { return eventId; }
    @Override public Instant occurredOn() { return occurredOn; }
    @Override public Optional<Identifier> aggregateId() { return Optional.empty(); }
}
