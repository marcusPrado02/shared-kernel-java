package com.marcusprado02.sharedkernel.domain.model.example.events;


import java.time.LocalDate;
import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.model.base.BaseDomainEvent;
import com.marcusprado02.sharedkernel.domain.model.base.Identifier;
import com.marcusprado02.sharedkernel.domain.model.example.entity.Subscription.SubscriptionId;

public final class SubscriptionRenewed extends BaseDomainEvent {
    private final SubscriptionId id;
    private final LocalDate newEndDate;

    public SubscriptionRenewed(SubscriptionId id, LocalDate newEndDate) {
        this.id = id;
        this.newEndDate = newEndDate;
    }

    @Override public String eventType() { return "subscription.renewed"; }
    @Override public Optional<Identifier> aggregateId() { return Optional.of(id); }
    public SubscriptionId subscriptionId() { return id; }
    public LocalDate newEndDate() { return newEndDate; }
}