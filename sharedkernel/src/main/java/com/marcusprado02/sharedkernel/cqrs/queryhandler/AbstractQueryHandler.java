package com.marcusprado02.sharedkernel.cqrs.queryhandler;


import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import com.marcusprado02.sharedkernel.cqrs.query.*;
import com.marcusprado02.sharedkernel.cqrs.queryhandler.ports.Authorizer;
import com.marcusprado02.sharedkernel.cqrs.queryhandler.ports.QueryCache;
import com.marcusprado02.sharedkernel.cqrs.queryhandler.ports.RateLimiter;
import com.marcusprado02.sharedkernel.cqrs.queryhandler.ports.ReadStoreSession;
import com.marcusprado02.sharedkernel.cqrs.queryhandler.ports.SortWhitelist;
import com.marcusprado02.sharedkernel.cqrs.queryhandler.support.ErrorMapper;
import com.marcusprado02.sharedkernel.cqrs.queryhandler.support.EtagCalculator;
import com.marcusprado02.sharedkernel.cqrs.queryhandler.support.KeyBuilders;

public abstract class AbstractQueryHandler<Q extends Query<R>, R> implements QueryHandler<Q, R> {

    protected final QueryHandlerOptions options;
    protected final ReadStoreSession readSession;
    protected final QueryCache cache;
    protected final Authorizer authz;
    protected final RateLimiter rateLimiter;
    protected final SortWhitelist sortWhitelist;
    protected final ErrorMapper errorMapper;
    protected final Function<R, String> etagFn;  // opcional para HTTP caching
    protected final java.util.function.Consumer<String> log;
    protected final java.util.function.BiConsumer<String, Throwable> logErr;

    protected AbstractQueryHandler(Builder<Q, R> b) {
        this.options = Objects.requireNonNull(b.options);
        this.readSession = Objects.requireNonNull(b.readSession);
        this.cache = Objects.requireNonNull(b.cache);
        this.authz = Objects.requireNonNull(b.authz);
        this.rateLimiter = Objects.requireNonNull(b.rateLimiter);
        this.sortWhitelist = Objects.requireNonNull(b.sortWhitelist);
        this.errorMapper = Objects.requireNonNullElse(b.errorMapper, ErrorMapper.defaultMapper());
        this.etagFn = Objects.requireNonNullElse(b.etagFn, EtagCalculator::weak);
        this.log = Objects.requireNonNullElse(b.log, s -> {});
        this.logErr = Objects.requireNonNullElse(b.logErr, (m,t) -> {});
    }

    public static class Builder<Q extends Query<R>, R> {
        private QueryHandlerOptions options = QueryHandlerOptions.strongReadCached();
        private ReadStoreSession readSession;
        private QueryCache cache;
        private Authorizer authz;
        private RateLimiter rateLimiter;
        private SortWhitelist sortWhitelist = () -> java.util.List.of();
        private ErrorMapper errorMapper;
        private java.util.function.Function<R,String> etagFn;
        private java.util.function.Consumer<String> log;
        private java.util.function.BiConsumer<String,Throwable> logErr;

        public Builder<Q,R> options(QueryHandlerOptions v){ this.options=v; return this; }
        public Builder<Q,R> readSession(ReadStoreSession v){ this.readSession=v; return this; }
        public Builder<Q,R> cache(QueryCache v){ this.cache=v; return this; }
        public Builder<Q,R> authz(Authorizer v){ this.authz=v; return this; }
        public Builder<Q,R> rateLimiter(RateLimiter v){ this.rateLimiter=v; return this; }
        public Builder<Q,R> sortWhitelist(SortWhitelist v){ this.sortWhitelist=v; return this; }
        public Builder<Q,R> errorMapper(ErrorMapper v){ this.errorMapper=v; return this; }
        public Builder<Q,R> etag(Function<R,String> v){ this.etagFn=v; return this; }
        public Builder<Q,R> log(java.util.function.Consumer<String> v){ this.log=v; return this; }
        public Builder<Q,R> logErr(java.util.function.BiConsumer<String,Throwable> v){ this.logErr=v; return this; }
    }

    @Override
    public final CompletionStage<R> handle(Q query, QueryMetadata md) {
        try {
            // 0) Consistência mínima
            var desired = strongerOf(md.consistency(), options.minConsistency());
            readSession.applyConsistency(desired);
            md = QueryMetadata.builder()
                    .correlationId(md.correlationId()).tenantId(md.tenantId()).userId(md.userId())
                    .traceparent(md.traceparent()).consistency(desired)
                    .attributes(md.attributes()).timestampUtc(md.timestampUtc())
                    .build();

            // 1) Rate limit por tenant/user/query
            try { rateLimiter.acquire(rateKey(query, md)); } catch (InterruptedException ignored) {}

            // 2) Autorização
            if (options.enforceAuthorization() && !requiredPermissions(query).isEmpty()) {
                var uid = md.userId();
                if (uid == null || !authz.hasAll(uid, requiredPermissions(query)))
                    return CompletableFuture.failedFuture(new SecurityException("Permissões insuficientes: " + requiredPermissions(query)));
            }

            // 3) Cache (condicional a hint de consistência)
            var useCache = options.enableCaching() && desired != ConsistencyHint.STRONG;
            var key = useCache ? KeyBuilders.cacheKey(query, md, options.cachePerUser()) : null;
            if (useCache) {
                var cached = cache.get(key);
                if (cached.isPresent()) {
                    log.accept("Cache HIT " + key + " replica=" + readSession.currentReplica().orElse("n/a"));
                    return CompletableFuture.completedFuture((R) cached.get());
                }
                log.accept("Cache MISS " + key);
            }

            // 4) Whitelists auxiliares (opcional)
            validateSortProjection(query, md);

            // 5) Core de leitura
            var started = System.nanoTime();
            var resultStage = doHandle(query, md);

            return resultStage.whenComplete((res, err) -> {
                var tookMs = (System.nanoTime() - started) / 1_000_000;
                if (err != null) {
                    var mapped = errorMapper.map(err);
                    logErr.accept("Query FAIL code=" + mapped.code() + " tookMs=" + tookMs, (Throwable) err);
                } else {
                    log.accept("Query OK etag=" + (res != null ? etagFn.apply(res) : "null") + " tookMs=" + tookMs);
                    if (useCache && res != null) cache.put(key, res, Math.toIntExact(Math.max(1, options.cacheTtl().toSeconds())));
                }
            });

        } catch (Throwable t) {
            return CompletableFuture.failedFuture(t);
        }
    }

    private static ConsistencyHint strongerOf(ConsistencyHint a, ConsistencyHint b){
        // STRONG > READ_COMMITTED > AT_MOST_STALE
        var rank = java.util.Map.of(ConsistencyHint.AT_MOST_STALE, 0, ConsistencyHint.READ_COMMITTED, 1, ConsistencyHint.STRONG, 2);
        return rank.get(a) >= rank.get(b) ? a : b;
    }

    /** Permissões necessárias (por query). */
    protected Set<String> requiredPermissions(Q query){ return Set.of(); }

    /** Valida sort/projection de forma canônica (override se quiser lógica própria). */
    protected void validateSortProjection(Q query, QueryMetadata md) { /* no-op */ }

    /** Implementar: busca de dados/projeções com os _ports_ adequados. */
    protected abstract CompletionStage<R> doHandle(Q query, QueryMetadata md) throws Exception;

    private String rateKey(Q q, QueryMetadata md){
        return "rate:" + q.getClass().getSimpleName() + ":" + (md.tenantId() == null ? "t?" : md.tenantId());
    }
}
