package com.marcusprado02.sharedkernel.infrastructure.retry;

import java.time.Duration;

public record RetryDecision(
    boolean shouldRetry,
    int nextAttempt,
    Duration backoffDelay,
    String reason
) {
    public static RetryDecision stop(String reason) {
        return new RetryDecision(false, -1, Duration.ZERO, reason);
    }
    public static RetryDecision retry(int nextAttempt, Duration delay, String reason) {
        return new RetryDecision(true, nextAttempt, delay, reason);
    }
}

