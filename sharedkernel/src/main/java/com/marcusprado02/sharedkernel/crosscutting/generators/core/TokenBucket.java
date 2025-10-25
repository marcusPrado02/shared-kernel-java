package com.marcusprado02.sharedkernel.crosscutting.generators.core;

final class TokenBucket implements RateLimiter {
    private final long capacity; private final long refillPerSecond; private double tokens; private long lastRefillNs;
    public TokenBucket(long capacity, long refillPerSecond) {
        this.capacity=capacity; this.refillPerSecond=refillPerSecond; this.tokens=capacity; this.lastRefillNs=System.nanoTime();
    }
    @Override public synchronized boolean tryAcquire() {
        long now = System.nanoTime();
        double toAdd = (now - lastRefillNs) / 1_000_000_000d * refillPerSecond;
        tokens = Math.min(capacity, tokens + toAdd); lastRefillNs = now;
        if (tokens >= 1d) { tokens -= 1d; return true; }
        return false;
    }
}
