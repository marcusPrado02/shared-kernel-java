package com.marcusprado02.sharedkernel.infrastructure.sms.core;


import java.time.Duration;
import java.util.concurrent.Callable;

final class FixedRetry implements RetryPolicy {
    private final Duration delay;
    private final int maxAttempts;

    FixedRetry(Duration delay, int maxAttempts) {
        this.delay = delay;
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    @Override public <T> T executeWithRetry(Callable<T> action) {
        RuntimeException last = null;
        for (int i = 0; i < maxAttempts; i++) {
            try { return action.call(); }
            catch (RuntimeException re) { last = re; sleep(); }
            catch (Exception e) { last = new RuntimeException(e); sleep(); }
        }
        throw last != null ? last : new RuntimeException("Retry failed without exception");
    }

    private void sleep() {
        try { Thread.sleep(delay.toMillis()); } catch (InterruptedException ignored) {}
    }
}
