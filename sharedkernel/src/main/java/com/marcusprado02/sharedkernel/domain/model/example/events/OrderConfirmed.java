package com.marcusprado02.sharedkernel.domain.model.example.events;


import java.time.Instant;
import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.model.base.BaseDomainEvent;
import com.marcusprado02.sharedkernel.domain.model.base.Identifier;
import com.marcusprado02.sharedkernel.domain.model.example.entity.OrderId;
import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

public final class OrderConfirmed extends BaseDomainEvent {
    private final OrderId orderId;
    private final Money total;
    private final Instant at;
    public OrderConfirmed(OrderId orderId, Money total, Instant at) {
        this.orderId = orderId; this.total = total; this.at = at;
    }
    @Override public String eventType() { return "order.confirmed"; }
    @Override public Optional<Identifier> aggregateId() { return Optional.of(orderId); }
    public OrderId orderId() { return orderId; }
    public Money total() { return total; }
    public Instant at() { return at; }
}