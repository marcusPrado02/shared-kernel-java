package com.marcusprado02.sharedkernel.saga.example.Impl;

import com.marcusprado02.sharedkernel.events.domain.DomainEvent;
import java.time.Instant;
import java.util.Map;

public class Events {
    public record InventoryReserved(String id, String correlationId, String orderId) implements DomainEvent { 
        public String type(){return "InventoryReserved";}

        @Override
        public String eventName() {
            return this.type();
        }

        @Override
        public String aggregateType() {
            return "Order";
        }

        @Override
        public String aggregateId() {
            return orderId;
        }

        @Override
        public Instant occurredOn() {
            return Instant.now();
        }

        @Override
        public Object data() {
            return Map.of(
                "id", id,
                "correlationId", correlationId,
                "orderId", orderId
            );
        }

        @Override
        public String source() {
            return correlationId;
        }
    }
    public record InventoryRejected(String id, String correlationId, String orderId, String reason) implements DomainEvent { 
        public String type(){return "InventoryRejected";}

        @Override
        public String eventName() {
            return this.type();
        }

        @Override
        public String aggregateType() {
            return "Order";
        }

        @Override
        public String aggregateId() {
            return orderId;
        }

        @Override
        public Instant occurredOn() {
            return Instant.now();
        }

        @Override
        public Object data() {
            return Map.of(
                "id", id,
                "correlationId", correlationId,
                "orderId", orderId,
                "reason", reason
            );
        }

        @Override
        public String source() {
            return correlationId;
        }
    }
    public record PaymentCharged(String id, String correlationId, String orderId) implements DomainEvent { 
        public String type(){return "PaymentCharged";}

        @Override
        public String eventName() {
            return this.type();
        }

        @Override
        public String aggregateType() {
            return "Order";
        }

        @Override
        public String aggregateId() {
            return orderId;
        }

        @Override
        public Instant occurredOn() {
            return Instant.now();
        }

        @Override
        public Object data() {
            return Map.of(
                "id", id,
                "correlationId", correlationId,
                "orderId", orderId
            );
        }

        @Override
        public String source() {
            return correlationId;
        }
    }
    public record PaymentDeclined(String id, String correlationId, String orderId) implements DomainEvent { 
        public String type(){return "PaymentDeclined";}

        @Override
        public String eventName() {
            return this.type();
        }

        @Override
        public String aggregateType() {
            return "Order";
        }

        @Override
        public String aggregateId() {
            return orderId;
        }

        @Override
        public Instant occurredOn() {
            return Instant.now();
        }

        @Override
        public Object data() {
            return Map.of(
                "id", id,
                "correlationId", correlationId,
                "orderId", orderId
            );
        }

        @Override
        public String source() {
            return correlationId;
        }
    }
    public record ShippingArranged(String id, String correlationId, String orderId, String tracking) implements DomainEvent { 
        public String type(){return "ShippingArranged";}

        @Override
        public String eventName() {
            return this.type();
        }

        @Override
        public String aggregateType() {
            return "Order";
        }

        @Override
        public String aggregateId() {
            return orderId;
        }

        @Override
        public Instant occurredOn() {
            return Instant.now();
        }

        @Override
        public Object data() {
            return Map.of(
                "id", id,
                "correlationId", correlationId,
                "orderId", orderId,
                "tracking", tracking
            );
        }

        @Override
        public String source() {
            return correlationId;
        }
    }
}
