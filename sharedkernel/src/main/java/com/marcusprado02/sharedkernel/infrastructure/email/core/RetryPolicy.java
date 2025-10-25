package com.marcusprado02.sharedkernel.infrastructure.email.core;

import java.time.Duration;
import java.util.concurrent.Callable;

public interface RetryPolicy {
    <T> T executeWithRetry(Callable<T> c);
    static RetryPolicy fixed(Duration d, int n){ return new FixedRetry(d, n); }
}