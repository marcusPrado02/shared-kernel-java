package com.marcusprado02.sharedkernel.infrastructure.payments.core;

import java.time.Duration;
import java.util.function.Supplier;

class SimpleRetry implements RetryPolicy {
    private final Duration delay; private final int attempts;
    SimpleRetry(Duration d, int a) { this.delay = d; this.attempts = a; }
    public <T> T executeWithRetry(Supplier<T> action) {
        RuntimeException last = null;
        for (int i=0; i<attempts; i++) {
            try { return action.get(); } catch (RuntimeException ex) {
                last = ex;
                try { Thread.sleep(delay.toMillis()); } catch (InterruptedException ignored) {}
            }
        }
        throw last != null ? last : new RuntimeException("Unknown retry failure");
    }
}
