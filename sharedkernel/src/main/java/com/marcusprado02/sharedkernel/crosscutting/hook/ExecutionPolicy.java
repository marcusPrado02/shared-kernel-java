package com.marcusprado02.sharedkernel.crosscutting.hook;


import java.time.Duration;

public record ExecutionPolicy(
    int maxAttempts,
    Duration baseBackoff,
    Duration maxBackoff,
    Duration timeout,
    boolean isolate,                    // executa em virtual thread
    boolean swallowErrors,              // não propaga
    RateLimiter rateLimiter,
    CircuitBreaker circuitBreaker
) {
    public static Builder builder(){ return new Builder(); }
    public static final class Builder {
        private int maxAttempts=1;
        private Duration baseBackoff=Duration.ofMillis(20);
        private Duration maxBackoff=Duration.ofMillis(200);
        private Duration timeout=Duration.ofSeconds(5);
        private boolean isolate=true, swallow=false;
        private RateLimiter limiter=RateLimiter.NOOP;
        private CircuitBreaker breaker=CircuitBreaker.NOOP;
        public Builder attempts(int n){this.maxAttempts=n;return this;}
        public Builder backoff(Duration base, Duration max){this.baseBackoff=base;this.maxBackoff=max;return this;}
        public Builder timeout(Duration t){this.timeout=t;return this;}
        public Builder isolate(boolean i){this.isolate=i;return this;}
        public Builder swallowErrors(boolean s){this.swallow=s;return this;}
        public Builder rateLimiter(RateLimiter rl){this.limiter=rl;return this;}
        public Builder circuitBreaker(CircuitBreaker cb){this.breaker=cb;return this;}
        public ExecutionPolicy build(){ return new ExecutionPolicy(maxAttempts, baseBackoff, maxBackoff, timeout, isolate, swallow, limiter, breaker); }
    }
}

