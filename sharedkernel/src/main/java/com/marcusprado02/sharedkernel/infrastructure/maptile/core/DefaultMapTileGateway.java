// src/main/java/com/marcusprado02/sharedkernel/infrastructure/maptile/core/DefaultMapTileGateway.java
package com.marcusprado02.sharedkernel.infrastructure.maptile.core;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.maptile.api.AttributionPolicy;
import com.marcusprado02.sharedkernel.infrastructure.maptile.api.MapTileGateway;
import com.marcusprado02.sharedkernel.infrastructure.maptile.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.maptile.api.StyleRegistry;
import com.marcusprado02.sharedkernel.infrastructure.maptile.api.TileRouter;
import com.marcusprado02.sharedkernel.infrastructure.maptile.model.*;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.*;

public class DefaultMapTileGateway implements MapTileGateway {

    private final Map<String, ProviderAdapter> providers = new HashMap<>();
    private final TileRouter router;
    private final Cache cache;
    private final InflightDedupe inflight;
    private final StyleRegistry styles;
    private final AttributionPolicy attribution;
    private final Map<String, Capabilities> caps = new HashMap<>();

    public DefaultMapTileGateway(Collection<ProviderFactory> factories,
                                 ProviderConfigResolver cfgResolver,
                                 TileRouter router, Cache cache,
                                 InflightDedupe inflight,
                                 StyleRegistry styles,
                                 AttributionPolicy attribution) {
        this.router = router; this.cache = cache; this.inflight = inflight;
        this.styles = styles; this.attribution = attribution;

        for (ProviderFactory f : factories) {
            ProviderConfig cfg = cfgResolver.resolve(f.providerId());
            ProviderAdapter adapter = f.create(cfg);
            providers.put(f.providerId(), adapter);
            caps.put(f.providerId(), adapter.metadata().capabilities());
        }
    }

    private ProviderAdapter pick(TileContext ctx){
        String id = Optional.ofNullable(ctx.preferredProvider()).orElseGet(() -> router.route(ctx));
        ProviderAdapter p = providers.get(id);
        if (p == null) throw new IllegalArgumentException("Provider não encontrado: " + id);
        return p;
    }

    @Override
    public TileData getTile(TileKey key, TileContext ctx, Policy policy) {
        if (policy.enforceAttribution()) attribution.validate(pick(ctx).metadata().id(), ctx);

        String ckey = key.cacheKey(null);
        if (policy.cacheRead()) {
            var cached = cache.get(ckey);
            if (cached.isPresent()) return cached.get();
        }

        // Encadeia limiter -> circuit -> retry, com Callable<TileData> e cast explícito
        TileData data = (TileData) policy.limiter().acquire(() ->
            policy.circuit().protect(() ->
                policy.retry().executeWithRetry(() -> pick(ctx).fetch(key, ctx, policy))
            )
        );

        if (policy.cacheWrite()) cache.put(ckey, data, data.meta().ttlSeconds());
        return data;
    }

    @Override
    public CompletableFuture<TileData> getTileAsync(TileKey key, TileContext ctx, Policy p) {
        String ckey = key.cacheKey(null);
        if (p.cacheRead()) {
            var cached = cache.get(ckey);
            if (cached.isPresent()) return CompletableFuture.completedFuture(cached.get());
        }
        return inflight.compute(ckey, () -> getTile(key, ctx, p));
    }

    @Override
    public RasterTile render(TileKey key, StyleRef styleRef, TileContext ctx, Policy p) {
        var style = styles.resolve(styleRef.styleId());
        TileData data = getTile(key, ctx, p);

        if (data instanceof RasterTile r) {
            if (p.cacheWrite()) cache.put(key.cacheKey(styleRef.styleId()), r, r.meta().ttlSeconds());
            return r;
        }

        // Vector -> Raster via façade MapTiles
        var decoded = MapTiles.VectorTileDecoder.decode(((VectorTile) data).pbf());
        var img = MapTiles.StyleEngine.render(decoded, style, key, ctx);
        var raster = new RasterTile(img.bytes(), img.contentType(), data.meta());
        if (p.cacheWrite()) cache.put(key.cacheKey(styleRef.styleId()), raster, data.meta().ttlSeconds());
        return raster;
    }

    @Override public CompletableFuture<RasterTile> renderAsync(TileKey key, StyleRef style, TileContext ctx, Policy p) {
        return CompletableFuture.supplyAsync(() -> render(key, style, ctx, p));
    }

    @Override public void prefetch(TileRange range, TileContext ctx, Policy p) {
        pick(ctx).prefetch(range, ctx, p);
    }

    @Override
    public OfflinePackResult buildOfflinePack(String packId, TileRange range, TileContext ctx) {
        // Usa façade para construir o pacote offline; fetcher chama getTile com policy default
        Policy defaults = Policy.defaults(
                RetryPolicy.fixed(java.time.Duration.ofMillis(100), 3),
                CircuitBreaker.noOp(),
                RateLimiter.noop()
        );
        return MapTiles.OfflinePackService.build(packId, range, t -> getTile(t, ctx, defaults));
    }

    @Override public void invalidate(TileKey key, TileContext ctx) {
        cache.invalidate(key.cacheKey(null));
    }

    @Override public Optional<String> resolveProvider(TileContext ctx){ return Optional.ofNullable(router.route(ctx)); }
    @Override public Capabilities capabilities(String providerId){ return caps.get(providerId); }
}
