package com.marcusprado02.sharedkernel.infrastructure.maptile.api;

import com.marcusprado02.sharedkernel.infrastructure.maptile.core.*;

public record Policy(
        RetryPolicy retry,
        CircuitBreaker circuit,
        RateLimiter limiter,
        boolean cacheRead, boolean cacheWrite, boolean enforceAttribution
) {
    public static Policy defaults(RetryPolicy r, CircuitBreaker c, RateLimiter l){
        return new Policy(r,c,l,true,true,true);
    }
}
