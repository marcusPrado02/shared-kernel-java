package com.marcusprado02.sharedkernel.crosscutting.generators.core;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

final class SimpleCircuitBreaker {
    private final int failureThreshold; private final Duration openDuration;
    private final AtomicLong openUntilEpochMs = new AtomicLong(0);
    private volatile int consecutiveFailures = 0;

    public SimpleCircuitBreaker(int failureThreshold, Duration openDuration) {
        this.failureThreshold=failureThreshold; this.openDuration=openDuration;
    }
    public synchronized <T> T call(SupplierWithEx<T> supplier) throws Exception {
        long now=System.currentTimeMillis();
        if (now < openUntilEpochMs.get()) throw new GenerationException("Circuit open");
        try {
            T r = supplier.get();
            consecutiveFailures=0; return r;
        } catch (Exception e) {
            if (++consecutiveFailures >= failureThreshold) {
                openUntilEpochMs.set(now + openDuration.toMillis());
            }
            throw e;
        }
    }
    @FunctionalInterface interface SupplierWithEx<T>{ T get() throws Exception; }
}

