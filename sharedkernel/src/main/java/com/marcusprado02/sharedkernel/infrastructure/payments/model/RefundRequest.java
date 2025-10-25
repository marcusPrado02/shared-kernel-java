package com.marcusprado02.sharedkernel.infrastructure.payments.model;

public record RefundRequest(
        String paymentId, Money amount, String reason, PaymentContext context, String idempotencyKey
) {}
