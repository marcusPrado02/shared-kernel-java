package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.time.Instant;
import java.util.Map;

public record PaymentResponse(
        String paymentId, PaymentStatus status, PaymentErrorCode error,
        String providerId, String providerChargeId, String approvalCode,
        String threeDSStatus, Map<String, Object> raw,
        Instant createdAt, Instant updatedAt
) {}

