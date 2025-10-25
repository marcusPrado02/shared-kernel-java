package com.marcusprado02.sharedkernel.infrastructure.payments.core;

import java.time.Duration;
import java.util.function.Supplier;

public interface RetryPolicy {
    <T> T executeWithRetry(Supplier<T> action);
    static RetryPolicy fixed(Duration delay, int maxAttempts) {
        return new SimpleRetry(delay, maxAttempts);
    }
}
