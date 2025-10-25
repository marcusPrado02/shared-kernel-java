package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.util.Map;

/** Detalhes enxutos de um pagamento no provider. Expanda conforme a sua necessidade. */
public record PaymentDetails(
        String paymentId,
        String providerId,
        Map<String, Object> raw
) {}
