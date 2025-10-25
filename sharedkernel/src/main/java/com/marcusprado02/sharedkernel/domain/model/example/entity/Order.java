package com.marcusprado02.sharedkernel.domain.model.example.entity;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import com.marcusprado02.sharedkernel.domain.model.base.AggregateRoot;
import com.marcusprado02.sharedkernel.domain.model.base.IdGenerator;
import com.marcusprado02.sharedkernel.domain.model.base.TenantId;
import com.marcusprado02.sharedkernel.domain.model.base.Version;
import com.marcusprado02.sharedkernel.domain.model.example.entity.OrderItem.ItemId;
import com.marcusprado02.sharedkernel.domain.model.example.events.OrderEvents;
import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

import jakarta.validation.constraints.NotNull;

import static com.marcusprado02.sharedkernel.domain.model.base.Guard.notNull;
import static com.marcusprado02.sharedkernel.domain.model.base.Guard.that;


public final class Order extends AggregateRoot<OrderId> {

    public enum Status { DRAFT, CONFIRMED, PAID, SHIPPED, CANCELLED }

    private Status status;
    private final Map<OrderItem.ItemId, OrderItem> items = new LinkedHashMap<>();
    private Money total; // cache coerente
    public static record Line(String sku, int qty, BigDecimal unitPrice) { }

    private Order(OrderId id) {
        super(id);
        this.status = Status.DRAFT;
        this.total  = Money.of(java.math.BigDecimal.ZERO, java.util.Currency.getInstance("USD"));
    }

    public Order(String orderId, String customerId, Object aggLines) {
        super(new OrderId(orderId));
        this.status = Status.DRAFT;
        this.total  = Money.of(java.math.BigDecimal.ZERO, java.util.Currency.getInstance("USD"));
    }
    

    public static Order createNew(IdGenerator<OrderId> idGen, TenantId tenant, String actor) {
        var o = new Order(notNull(idGen, "idGen").newId());
        o.setTenant(tenant);
        return o.mutate(actor, () -> { o.recalculateTotal(); return o; });
    }

    // ========== Comportamentos do agregado ==========
    public void addItem(OrderItem item, String actor) {
        mutate(actor, () -> {
            that(status == Status.DRAFT || status == Status.CONFIRMED, () -> "cannot add item in status " + status);
            notNull(item, "item");
            items.merge(item.id(), item, (oldV, newV) -> { oldV.increase(newV.quantity()); return oldV; });
            recalculateTotal();
            publishEvent(new OrderEvents.ItemAdded(id(), item.id(), item.sku(), item.quantity(), item.unitPrice(), Instant.now()));
        });
    }

    public void removeItem(OrderItem.ItemId itemId, String actor) {
        mutate(actor, () -> {
            that(status == Status.DRAFT || status == Status.CONFIRMED, () -> "cannot remove item in status " + status);
            var removed = items.remove(notNull(itemId, "itemId"));
            that(removed != null, () -> "item not found");
            recalculateTotal();
            publishEvent(new OrderEvents.ItemRemoved(id(), itemId, Instant.now()));
        });
    }

    public void confirm(String actor) {
        mutate(actor, () -> {
            that(status == Status.DRAFT, () -> "only DRAFT can be confirmed");
            that(!items.isEmpty(), () -> "cannot confirm empty order");
            this.status = Status.CONFIRMED;
            publishEvent(new OrderEvents.OrderConfirmed(id(), total, Instant.now()));
        });
    }

    /** Idempotente por paymentKey — útil quando pagamento vem via EDA/Saga. */
    public void markPaid(String paymentKey, String actor) {
        once("PAY:"+notNull(paymentKey, "paymentKey"), () ->
            mutate(actor, () -> {
                that(status == Status.CONFIRMED, () -> "only CONFIRMED can be paid");
                this.status = Status.PAID;
                publishEvent( new OrderEvents.OrderPaid(id(), total, paymentKey, Instant.now()));
            })
        );
    }

    public void ship(String trackingCode, String actor) {
        mutate(actor, () -> {
            that(status == Status.PAID, () -> "only PAID can be shipped");
            that(trackingCode != null && !trackingCode.isBlank(), () -> "tracking required");
            this.status = Status.SHIPPED;
            publishEvent(new OrderEvents.OrderShipped(id(), trackingCode, Instant.now()));
        });
    }

    public void cancel(String reason, String actor) {
        mutate(actor, () -> {
            that(status != Status.CANCELLED && status != Status.SHIPPED, () -> "cannot cancel shipped/cancelled");
            this.status = Status.CANCELLED;
            publishEvent(new OrderEvents.OrderCancelled(id(), reason, Instant.now()));
        });
    }

    // ========== Regras do agregado ==========
    @Override protected void validateAggregate() {
        // exemplo de invariantes:
        that(total != null, () -> "total null");
        that(total.amount().signum() >= 0, () -> "total negative");
        // caso queira: limite de itens, limites por SKU, etc.
    }

    private void recalculateTotal() {
        Money acc = null;
        for (var it : items.values()) {
            acc = (acc == null) ? it.lineTotal() : acc.plus(it.lineTotal());
        }
        if (acc == null) {
            acc = Money.of(java.math.BigDecimal.ZERO, java.util.Currency.getInstance("USD"));
        }
        this.total = acc;
    }

    // ========== Reconstituição (a partir do repo) ==========
    public static Order reconstitute(OrderId id, Version v, TenantId t, Instant c, Instant u, String cb, String ub, Instant d,
                                     Status status, Collection<OrderItem> restoredItems, Money total) {
        var o = new Order(id);
        o._onPersistedNewVersion(v);
        o.setTenant(t);
        // set audit fields
        // (como estão protegidos, fazemos via construtor protected do Entity se preferir um caminho dedicado)
        // aqui vamos simular via toques:
        o.touch();
        o.status = status;
        restoredItems.forEach(it -> o.items.put(it.id(), it));
        o.total = total;
        o.validateAggregate();
        return o;
    }

    // getters controlados
    public Status status() { return status; }
    public Money total() { return total; }
    public Collection<OrderItem> items() { return Collections.unmodifiableCollection(items.values()); }
}