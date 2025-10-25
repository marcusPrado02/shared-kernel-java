package com.marcusprado02.sharedkernel.infrastructure.messaging;

/** Política de retry. */
public record RetryPolicy(
    int maxAttempts,
    long initialBackoffMs,
    double multiplier,
    long maxBackoffMs
) {
  public static RetryPolicy exponential() { return new RetryPolicy(5, 200, 2.0, 30_000); }
}
