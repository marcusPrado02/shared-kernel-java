package com.marcusprado02.sharedkernel.cqrs.bulk.spi;

public interface RateLimiter { void acquire(); // noop por default
    static RateLimiter noop(){ return () -> {}; } }
