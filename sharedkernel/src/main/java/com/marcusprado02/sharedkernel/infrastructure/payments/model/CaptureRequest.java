package com.marcusprado02.sharedkernel.infrastructure.payments.model;


public record CaptureRequest(
        String paymentId, Money amount, PaymentContext context, String idempotencyKey
) {}