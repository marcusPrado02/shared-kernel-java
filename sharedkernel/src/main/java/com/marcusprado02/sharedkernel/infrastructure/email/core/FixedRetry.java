package com.marcusprado02.sharedkernel.infrastructure.email.core;


import java.time.Duration;
import java.util.concurrent.Callable;

/** Retry simples com N tentativas e intervalo fixo entre elas. */
final class FixedRetry implements RetryPolicy {
    private final Duration delay;
    private final int maxAttempts;

    FixedRetry(Duration delay, int maxAttempts) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        this.delay = (delay == null ? Duration.ZERO : delay);
        this.maxAttempts = maxAttempts;
    }

    @Override
    public <T> T executeWithRetry(Callable<T> c) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return c.call();
            } catch (RuntimeException re) {
                last = re;
            } catch (Exception e) {
                last = new RuntimeException(e);
            }
            if (attempt < maxAttempts && !delay.isZero() && !delay.isNegative()) {
                try { Thread.sleep(delay.toMillis()); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
        // esgota tentativas
        throw last != null ? last : new RuntimeException("Retry failed with unknown error");
    }
}