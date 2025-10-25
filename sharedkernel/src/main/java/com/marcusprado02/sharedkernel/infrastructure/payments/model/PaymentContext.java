package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.util.Map;

public record PaymentContext(
        String tenantId, String storeId, String country, String currency,
        String preferredProvider, Map<String, Object> tags
) {}
