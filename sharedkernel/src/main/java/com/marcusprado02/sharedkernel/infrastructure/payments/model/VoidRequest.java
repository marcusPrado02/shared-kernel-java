package com.marcusprado02.sharedkernel.infrastructure.payments.model;

public record VoidRequest(String paymentId, PaymentContext context, String idempotencyKey) {}

