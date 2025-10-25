package com.marcusprado02.sharedkernel.crosscutting.generators.core;

// Decorator de resiliência
public final class ResilientGenerator<T> implements Generator<T> {
    private final Generator<T> delegate;
    private final RetryPolicy retry;
    private final RateLimiter limiter;
    private final SimpleCircuitBreaker breaker;

    public ResilientGenerator(Generator<T> d, RetryPolicy r, RateLimiter l, SimpleCircuitBreaker b){
        this.delegate=d; this.retry=r; this.limiter=l; this.breaker=b;
    }

    @Override public T generate(GenerationContext ctx) {
        if (limiter!=null && !limiter.tryAcquire()) throw new GenerationException("Rate limit exceeded");
        try {
            return retry!=null
                    ? retry.execute(() -> breaker!=null ? breaker.call(() -> delegate.generate(ctx)) : delegate.generate(ctx))
                    : (breaker!=null ? breaker.call(() -> delegate.generate(ctx)) : delegate.generate(ctx));
        } catch (RuntimeException re) { throw re;
        } catch (Exception e) { throw new GenerationException("Generation failed", e); }
    }
}