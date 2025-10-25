package com.marcusprado02.sharedkernel.cqrs.queryhandler;


import java.time.Duration;

import com.marcusprado02.sharedkernel.cqrs.query.ConsistencyHint;

public record QueryHandlerOptions(
        boolean enforceAuthorization,
        boolean enableCaching,
        Duration cacheTtl,
        boolean cachePerUser,              // parte da cache key
        boolean whitelistSort,
        boolean whitelistProjection,
        ConsistencyHint minConsistency,    // mínimo aceito pelo handler
        boolean emitMetrics,
        boolean emitTracing
) {
    public static QueryHandlerOptions strongReadCached(){
        return new QueryHandlerOptions(true, true, Duration.ofSeconds(30), true, true, true,
                ConsistencyHint.READ_COMMITTED, true, true);
    }
    public QueryHandlerOptions noCache(){ return new QueryHandlerOptions(enforceAuthorization, false, Duration.ZERO, cachePerUser, whitelistSort, whitelistProjection, minConsistency, emitMetrics, emitTracing); }
}
