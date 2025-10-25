package com.marcusprado02.sharedkernel.crosscutting.hook;

/** Abstrações simples de resiliência (pluggable). */
public interface RateLimiter { boolean tryAcquire(); RateLimiter NOOP = () -> true; }
