package com.marcusprado02.sharedkernel.cqrs.queryhandler.ports;

public interface RateLimiter {
    void acquire(String key) throws InterruptedException; // token-bucket/leaky-bucket
}
