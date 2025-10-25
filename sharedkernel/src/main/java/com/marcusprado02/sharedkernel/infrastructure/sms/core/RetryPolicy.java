package com.marcusprado02.sharedkernel.infrastructure.sms.core;


import java.util.concurrent.Callable;

public interface RetryPolicy {
    <T> T executeWithRetry(Callable<T> action);

    static RetryPolicy fixed(java.time.Duration delay, int maxAttempts) {
        return new FixedRetry(delay, maxAttempts);
    }
}