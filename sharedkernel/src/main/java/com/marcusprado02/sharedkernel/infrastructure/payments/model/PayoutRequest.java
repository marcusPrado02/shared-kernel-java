package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.util.Map;

public record PayoutRequest(
        String idempotencyKey, Money amount, String destinationToken,
        PaymentContext context, Map<String, Object> metadata
) {}
