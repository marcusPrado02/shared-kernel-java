package com.marcusprado02.sharedkernel.crosscutting.generators.core;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

final class ExponentialBackoffRetry implements RetryPolicy {
    private final int maxAttempts; private final Duration base; private final Duration max;
    private final Predicate<Throwable> retriable;
    ExponentialBackoffRetry(int maxAttempts, Duration base, Duration max, Predicate<Throwable> r){
        this.maxAttempts=maxAttempts; this.base=base; this.max=max; this.retriable=r;
    }
    public <T> T execute(CheckedSupplier<T> supplier) {
        int attempt=0;
        while (true) {
            try { return supplier.get(); }
            catch (Throwable t) {
                attempt++;
                if (attempt>=maxAttempts || !retriable.test(t)) {
                    if (t instanceof RuntimeException re) throw re;
                    throw new GenerationException("Retries exhausted", t);
                }
                long jitter = ThreadLocalRandom.current().nextLong(0, base.toMillis());
                long sleep = Math.min(max.toMillis(), (long)(base.toMillis() * Math.pow(2, attempt-1))) + jitter;
                try { Thread.sleep(sleep); } catch (InterruptedException ie){ Thread.currentThread().interrupt(); }
            }
        }
    }
}