package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.util.Map;

public record RefundResponse(
        String refundId, PaymentStatus status, PaymentErrorCode error,
        String providerId, String providerRefundId, Map<String, Object> raw
) {}

