package com.marcusprado02.sharedkernel.infrastructure.retry;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public interface RetryBudget {
    boolean tryConsume(RetryContext ctx);

    static RetryBudget tokenBucket(int capacity, int refillPerSecond) {
        return new TokenBucket(capacity, refillPerSecond);
    }
    static RetryBudget unlimited(){ return ctx -> true; }

    final class TokenBucket implements RetryBudget {
        private final int capacity;
        private final int refillPerSec;
        private final AtomicLong tokens = new AtomicLong(0);
        private volatile long lastRefillSec = Instant.now().getEpochSecond();

        TokenBucket(int capacity, int refillPerSecond) {
            this.capacity = capacity;
            this.refillPerSec = refillPerSecond;
            tokens.set(capacity);
        }
        @Override public boolean tryConsume(RetryContext ctx) {
            long now = Instant.now().getEpochSecond();
            long last = lastRefillSec;
            if (now > last) {
                long delta = now - last;
                long add = Math.min((long)capacity, delta * refillPerSec);
                tokens.updateAndGet(t -> Math.min(capacity, t + add));
                lastRefillSec = now;
            }
            long t = tokens.get();
            if (t <= 0) return false;
            return tokens.compareAndSet(t, t - 1);
        }
    }
}
