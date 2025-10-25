package com.marcusprado02.sharedkernel.infrastructure.payments.recon;

public record ReconciliationResult(
        String providerId, String providerChargeId, String internalPaymentId,
        boolean matched, String reason
) {}
