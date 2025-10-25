package com.marcusprado02.sharedkernel.saga;

import java.time.Duration;

public record RetryPolicy(int maxAttempts, Duration backoff, boolean exponential) {
    public static RetryPolicy linear(int max, Duration backoff){ return new RetryPolicy(max, backoff, false); }
    public static RetryPolicy expo(int max, Duration base){ return new RetryPolicy(max, base, true); }
}
