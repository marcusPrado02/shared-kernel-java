package com.marcusprado02.sharedkernel.infrastructure.sms.core;


import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/** Implementação simples estilo token-bucket. */
public interface RateLimiter {
    <T> T acquire(Callable<T> action);

    static RateLimiter noop() {
        return new RateLimiter() {
            @Override
            public <T> T acquire(Callable<T> action) {
                try { return action.call(); } catch (Exception e) { throw new RuntimeException(e); }
            }
        };
    }

    static RateLimiter tokenBucket(long capacity, Duration refillInterval) {
        return new TokenBucket(capacity, refillInterval);
    }

    class TokenBucket implements RateLimiter {
        private final long capacity;
        private final long refillNanos;
        private final AtomicLong tokens = new AtomicLong(0);
        private volatile long lastRefill = System.nanoTime();

        TokenBucket(long capacity, Duration refillInterval) {
            if (capacity <= 0) throw new IllegalArgumentException("capacity > 0");
            this.capacity = capacity;
            this.refillNanos = refillInterval.toNanos();
            this.tokens.set(capacity);
        }

        @Override public <T> T acquire(Callable<T> action) {
            refill();
            while (true) {
                long current = tokens.get();
                if (current > 0 && tokens.compareAndSet(current, current - 1)) break;
                // Sem token: espera um pouquinho e tenta de novo
                try { Thread.sleep(Math.min(5, Math.max(1, refillNanos / 1_000_000))); } catch (InterruptedException ignored) {}
                refill();
            }
            try { return action.call(); }
            catch (Exception e) { throw new RuntimeException(e); }
            finally { /* opcional: não devolvemos token, é por requisição */ }
        }

        private void refill() {
            long now = System.nanoTime();
            if (now - lastRefill >= refillNanos) {
                tokens.set(capacity);
                lastRefill = now;
            }
        }
    }
}

