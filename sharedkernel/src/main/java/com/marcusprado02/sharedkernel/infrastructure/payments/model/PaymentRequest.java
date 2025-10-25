package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.util.Map;

public record PaymentRequest(
        String idempotencyKey, Money amount, Customer customer,
        PaymentMethod method, PaymentContext context, RiskData risk,
        Map<String, Object> metadata, boolean capture // true => sale()
) {}
