package com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.local;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.*;

public final class LocalTokenBucketLimiter implements RateLimiter {
    private static final class Bucket {
        final int capacity;
        final long refillNanos;
        volatile long nextRefill = System.nanoTime();
        final AtomicInteger tokens;
        Bucket(int capacity, long windowNanos){
            this.capacity = capacity; this.refillNanos = windowNanos;
            this.tokens = new AtomicInteger(capacity);
        }
    }
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override public Decision evaluateAndConsume(RateKey key, LimitSpec spec) {
        var bk = buckets.computeIfAbsent(key.asString()+"|"+spec.name(),
                ignored -> new Bucket(Math.max(spec.localBurst(), 1), spec.window().toNanos()));
        refillIfNeeded(bk);
        int after = bk.tokens.decrementAndGet();
        if (after >= 0) {
            long reset = Instant.now().plusNanos(bk.refillNanos).getEpochSecond();
            return new Decision(true, after, reset, Map.of("local", true));
        } else {
            bk.tokens.incrementAndGet(); // rollback
            long reset = Instant.now().plusNanos(bk.refillNanos).getEpochSecond();
            return new Decision(false, 0, reset, Map.of("local", true));
        }
    }
    private void refillIfNeeded(Bucket b){
        long now = System.nanoTime();
        if (now >= b.nextRefill) {
            b.tokens.set(b.capacity);
            b.nextRefill = now + b.refillNanos;
        }
    }
}