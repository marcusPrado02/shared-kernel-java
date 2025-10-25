package com.marcusprado02.sharedkernel.infrastructure.payments.core;

public interface OutboxPublisher {
    void publishPaymentEvent(String type, Object payload); // "PAYMENT.AUTHORIZED"
}
