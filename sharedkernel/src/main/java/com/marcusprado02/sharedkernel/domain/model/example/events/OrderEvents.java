package com.marcusprado02.sharedkernel.domain.model.example.events;


import java.time.Instant;

import com.marcusprado02.sharedkernel.domain.model.base.BaseDomainEvent;
import com.marcusprado02.sharedkernel.domain.model.example.entity.Order;
import com.marcusprado02.sharedkernel.domain.model.example.entity.OrderId;
import com.marcusprado02.sharedkernel.domain.model.example.entity.OrderItem;
import com.marcusprado02.sharedkernel.domain.model.example.entity.OrderItem.ItemId;
import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

public final class OrderEvents {
    public static class ItemAdded extends BaseDomainEvent {
        private final OrderId orderId;
        private final ItemId itemId;
        private final String sku;
        private final int quantity;
        private final Money unitPrice;
        private final Instant when;

        public ItemAdded(OrderId orderId,
                         ItemId itemId,
                         String sku,
                         int quantity,
                         Money unitPrice,
                         Instant when) {
            this.orderId = orderId;
            this.itemId = itemId;
            this.sku = sku;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.when = when;
        }

        public OrderId orderId() { return orderId; }
        public OrderItem.ItemId itemId() { return itemId; }
        public String sku() { return sku; }
        public int quantity() { return quantity; }
        public Money unitPrice() { return unitPrice; }
        public Instant when() { return when; }

        @Override
        public String eventType() {
            return "order.item.added";
        }
    }

    public static class ItemRemoved extends BaseDomainEvent {
        private final OrderId orderId;
        private final ItemId itemId;
        private final Instant when;

        public ItemRemoved(OrderId orderId, ItemId itemId, Instant when) {
            this.orderId = orderId;
            this.itemId = itemId;
            this.when = when;
        }

        public OrderId orderId() { return orderId; }
        public OrderItem.ItemId itemId() { return itemId; }
        public Instant when() { return when; }

        @Override
        public String eventType() {
            return "order.item.removed";
        }
    }

    public static class OrderConfirmed extends BaseDomainEvent {
        private final OrderId orderId;
        private final Money total;
        private final Instant when;

        public OrderConfirmed(OrderId orderId, Money total, Instant when) {
            this.orderId = orderId;
            this.total = total;
            this.when = when;
        }

        public OrderId orderId() { return orderId; }
        public Money total() { return total; }
        public Instant when() { return when; }

        @Override
        public String eventType() {
            return "order.confirmed";
        }
    }

    public static class OrderPaid extends BaseDomainEvent {
        private final OrderId orderId;
        private final Money total;
        private final String paymentKey;
        private final Instant when;

        public OrderPaid(OrderId orderId, Money total, String paymentKey, Instant when) {
            this.orderId = orderId;
            this.total = total;
            this.paymentKey = paymentKey;
            this.when = when;
        }

        public OrderId orderId() { return orderId; }
        public Money total() { return total; }
        public String paymentKey() { return paymentKey; }
        public Instant when() { return when; }

        @Override
        public String eventType() {
            return "order.paid";
        }
    }

    public static class OrderShipped extends BaseDomainEvent {
        private final OrderId orderId;
        private final String trackingCode;
        private final Instant when;

        public OrderShipped(OrderId orderId, String trackingCode, Instant when) {
            this.orderId = orderId;
            this.trackingCode = trackingCode;
            this.when = when;
        }

        public OrderId orderId() { return orderId; }
        public String trackingCode() { return trackingCode; }
        public Instant when() { return when; }

        @Override
        public String eventType() {
            return "order.shipped";
        }
    }

    public static class OrderCancelled extends BaseDomainEvent {
        private final OrderId orderId;
        private final String reason;
        private final Instant when;

        public OrderCancelled(OrderId orderId, String reason, Instant when) {
            this.orderId = orderId;
            this.reason = reason;
            this.when = when;
        }

        public OrderId orderId() { return orderId; }
        public String reason() { return reason; }
        public Instant when() { return when; }

        @Override
        public String eventType() {
            return "order.cancelled";
        }
    }
}
