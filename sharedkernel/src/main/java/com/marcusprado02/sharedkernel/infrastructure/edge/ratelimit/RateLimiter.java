package com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit;

import java.util.Map;

public interface RateLimiter {
    /** Decide e, se permitido, consome 1 unidade. */
    Decision evaluateAndConsume(RateKey key, LimitSpec spec);
    record Decision(boolean allowed, long remaining, long resetEpochSeconds, Map<String,Object> debug) {}
}
