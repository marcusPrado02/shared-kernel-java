package com.marcusprado02.sharedkernel.domain.model.example.events;


import java.time.Instant;
import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.model.base.BaseDomainEvent;
import com.marcusprado02.sharedkernel.domain.model.base.Identifier;
import com.marcusprado02.sharedkernel.domain.model.example.entity.Subscription.SubscriptionId;

public final class SubscriptionCreated extends BaseDomainEvent {
    private final SubscriptionId id;
    private final Instant createdAt;

    public SubscriptionCreated(SubscriptionId id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    @Override public String eventType() { return "subscription.created"; }
    @Override public Optional<Identifier> aggregateId() { return Optional.of(id); }
    public SubscriptionId subscriptionId() { return id; }
    public Instant createdAt() { return createdAt; }
}