package com.marcusprado02.sharedkernel.infrastructure.maptile.core;

public interface RateLimiter<T> { T acquire(java.util.concurrent.Callable<T> c); static <T> RateLimiter<T> noop(){ return c -> { try { return c.call(); } catch (Exception e) { throw new RuntimeException(e); } }; } }

