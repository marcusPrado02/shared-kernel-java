package com.marcusprado02.sharedkernel.infrastructure.payments.api;

import com.marcusprado02.sharedkernel.infrastructure.payments.core.CircuitBreaker;
import com.marcusprado02.sharedkernel.infrastructure.payments.core.RetryPolicy;

public record Policy(
        RetryPolicy retryPolicy,
        CircuitBreaker circuitBreaker,
        boolean outboxEnabled,
        boolean auditEnabled
) {
    public static Policy defaults(RetryPolicy r, CircuitBreaker c) {
        return new Policy(r, c, true, true);
    }
}
