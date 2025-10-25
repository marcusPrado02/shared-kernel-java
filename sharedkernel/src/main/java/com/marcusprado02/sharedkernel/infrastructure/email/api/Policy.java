package com.marcusprado02.sharedkernel.infrastructure.email.api;

import com.marcusprado02.sharedkernel.infrastructure.email.core.*;

public record Policy(
        RetryPolicy retry,
        CircuitBreaker circuit,
        RateLimiter limiter,
        boolean outboxEnabled, boolean auditEnabled, boolean trackOpens, boolean trackClicks
) {
    public static Policy defaults(RetryPolicy r, CircuitBreaker c, RateLimiter l){
        return new Policy(r,c,l,true,true,true,true);
    }
}
