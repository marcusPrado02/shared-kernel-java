package com.marcusprado02.sharedkernel.adapters.in.ws.guard;


import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucket {
    private static class Bucket { int tokens; long last; }
    private final Map<String,Bucket> map = new ConcurrentHashMap<>();
    private final int capacity, refillPerSec;
    public TokenBucket(int capacity, int refillPerSec) { this.capacity=capacity; this.refillPerSec=refillPerSec; }

    public boolean tryConsume(String key) {
        var b = map.computeIfAbsent(key, k -> { var x = new Bucket(); x.tokens = capacity; x.last = Instant.now().getEpochSecond(); return x; });
        refill(b);
        if (b.tokens <= 0) return false;
        b.tokens--; return true;
    }
    private void refill(Bucket b) {
        long now = Instant.now().getEpochSecond();
        long delta = now - b.last;
        if (delta > 0) { b.tokens = Math.min(capacity, b.tokens + (int)(delta*refillPerSec)); b.last = now; }
    }
}