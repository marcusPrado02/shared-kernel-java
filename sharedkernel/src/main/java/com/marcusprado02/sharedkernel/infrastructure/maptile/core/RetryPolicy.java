package com.marcusprado02.sharedkernel.infrastructure.maptile.core;


import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Política de retry genérica para chamadas de rede em MapTile.
 */
public interface RetryPolicy {
    <T> T executeWithRetry(Callable<T> c);

    static RetryPolicy fixed(Duration delay, int maxAttempts) {
        return new SimpleRetry(delay, maxAttempts);
    }

    static RetryPolicy noOp() {
        return new RetryPolicy() {
            @Override
            public <T> T executeWithRetry(Callable<T> c) {
                try {
                    return c.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}