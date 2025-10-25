package com.marcusprado02.sharedkernel.cqrs.query.middleware;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.marcusprado02.sharedkernel.cqrs.query.ConsistencyHint;
import com.marcusprado02.sharedkernel.cqrs.query.*;
import com.marcusprado02.sharedkernel.cqrs.query.cache.QueryCache;

public final class CachingMiddleware implements QueryMiddleware {
    private final QueryCache cache; private final int ttlSeconds; private final Function<QueryEnvelope<?>, String> keyer;
    public CachingMiddleware(QueryCache cache, int ttlSeconds, Function<QueryEnvelope<?>, String> keyer) {
        this.cache = cache; this.ttlSeconds = ttlSeconds; this.keyer = keyer;
    }
    @Override public <R> java.util.concurrent.CompletionStage<QueryResult<R>> invoke(QueryEnvelope<R> env, Next next) {
        if (env.metadata().consistency() == ConsistencyHint.STRONG) return next.invoke(env); // bypass cache
        var key = keyer.apply(env);
        var cached = cache.get(key);
        if (cached.isPresent()) return CompletableFuture.completedFuture(QueryResult.of((R) cached.get()));
        return next.invoke(env).whenComplete((res, t) -> {
            if (t == null && res.value().isPresent()) cache.put(key, res.value().get(), ttlSeconds);
        });
    }
}
