package com.marcusprado02.sharedkernel.domain.model.example.events;


import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.model.base.BaseDomainEvent;
import com.marcusprado02.sharedkernel.domain.model.base.Identifier;
import com.marcusprado02.sharedkernel.domain.model.example.entity.Subscription.SubscriptionId;

public final class SubscriptionCancelled extends BaseDomainEvent {
    private final SubscriptionId id;
    private final String reason;

    public SubscriptionCancelled(SubscriptionId id, String reason) {
        this.id = id;
        this.reason = reason;
    }

    @Override public String eventType() { return "subscription.cancelled"; }
    @Override public Optional<Identifier> aggregateId() { return Optional.of(id); }
    public SubscriptionId subscriptionId() { return id; }
    public String reason() { return reason; }
}