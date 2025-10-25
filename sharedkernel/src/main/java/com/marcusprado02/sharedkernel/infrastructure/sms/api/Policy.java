package com.marcusprado02.sharedkernel.infrastructure.sms.api;

import com.marcusprado02.sharedkernel.infrastructure.sms.core.CircuitBreaker;
import com.marcusprado02.sharedkernel.infrastructure.sms.core.RateLimiter;
import com.marcusprado02.sharedkernel.infrastructure.sms.core.RetryPolicy;

public record Policy(RetryPolicy retry, CircuitBreaker circuit, RateLimiter rateLimiter,
                     boolean outboxEnabled, boolean auditEnabled) {
    public static Policy defaults(RetryPolicy r, CircuitBreaker c, RateLimiter rl) {
        return new Policy(r, c, rl, true, true);
    }
}
