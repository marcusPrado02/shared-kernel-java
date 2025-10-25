package com.marcusprado02.sharedkernel.observability.tracing.samplers.util;

import java.util.concurrent.ConcurrentHashMap;

public final class TokenBucket {
    private final double perSecond;
    private final int burst;
    private double tokens;
    private long lastNs;

    public TokenBucket(double perSecond, int burst) {
        this.perSecond = Math.max(0, perSecond);
        this.burst = Math.max(1, burst);
        this.tokens = burst;
        this.lastNs = System.nanoTime();
    }
    public synchronized boolean allow(){
        long now = System.nanoTime();
        double refill = (now - lastNs) / 1_000_000_000d * perSecond;
        tokens = Math.min(burst, tokens + refill);
        lastNs = now;
        if (tokens >= 1.0) { tokens -= 1.0; return true; }
        return false;
    }

    // “por chave”
    public static final class PerKey {
        private final double perSecond; private final int burst;
        private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
        public PerKey(double perSecond, int burst){ this.perSecond=perSecond; this.burst=burst; }
        public boolean allow(String key){ return buckets.computeIfAbsent(key, k -> new TokenBucket(perSecond, burst)).allow(); }
    }
}
