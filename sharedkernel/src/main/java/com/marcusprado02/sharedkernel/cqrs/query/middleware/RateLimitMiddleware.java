package com.marcusprado02.sharedkernel.cqrs.query.middleware;

import com.marcusprado02.sharedkernel.cqrs.query.*;

public final class RateLimitMiddleware implements QueryMiddleware {
    public interface RateLimiter { void acquire(String key) throws InterruptedException; }
    private final RateLimiter limiter; private final java.util.function.Function<QueryEnvelope<?>, String> keyer;
    public RateLimitMiddleware(RateLimiter limiter, java.util.function.Function<QueryEnvelope<?>, String> keyer){
        this.limiter = limiter; this.keyer = keyer;
    }
    @Override public <R> java.util.concurrent.CompletionStage<QueryResult<R>> invoke(QueryEnvelope<R> env, Next next) {
        try { limiter.acquire(keyer.apply(env)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return next.invoke(env);
    }
}
