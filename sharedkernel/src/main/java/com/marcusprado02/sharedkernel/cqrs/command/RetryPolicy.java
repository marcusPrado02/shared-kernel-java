package com.marcusprado02.sharedkernel.cqrs.command;

import java.time.Duration;

/** Política de retentativa com backoff exponencial opcional e jitter. */
public record RetryPolicy(int maxAttempts, Duration initialBackoff, double backoffMultiplier, boolean jitter) {
    public static RetryPolicy disabled(){ return new RetryPolicy(1, Duration.ZERO, 1.0, false); }
    public static RetryPolicy expo(int maxAttempts){ return new RetryPolicy(maxAttempts, Duration.ofMillis(100), 2.0, true); }
    public static RetryPolicy linear(int maxAttempts, Duration interval){ return new RetryPolicy(maxAttempts, interval, 1.0, false); }
}
