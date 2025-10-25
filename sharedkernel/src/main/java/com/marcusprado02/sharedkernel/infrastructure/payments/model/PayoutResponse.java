package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.util.Map;

public record PayoutResponse(
        String payoutId, PaymentStatus status, PaymentErrorCode error,
        String providerId, String providerPayoutId, Map<String, Object> raw
) {}
