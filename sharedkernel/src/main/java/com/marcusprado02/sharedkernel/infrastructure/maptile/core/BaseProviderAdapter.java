package com.marcusprado02.sharedkernel.infrastructure.maptile.core;


import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.maptile.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.maptile.model.*;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.ProviderAdapter;

public abstract class BaseProviderAdapter implements ProviderAdapter {

    /** Encadeia RateLimiter -> CircuitBreaker -> Retry. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> T run(Policy p, Callable<T> c){
        return (T) p.limiter().acquire(() -> p.circuit().protect(c));
    }

    /** Helper para construir RasterTile de sucesso. */
    protected RasterTile okRaster(byte[] bytes, String contentType, TileMeta meta){
        return new RasterTile(bytes, contentType, meta);
    }

    @Override
    public CompletableFuture<TileData> fetchAsync(TileKey key, TileContext ctx, Policy p) {
        return CompletableFuture.supplyAsync(() -> fetch(key, ctx, p));
    }

    @Override
    public void prefetch(TileRange range, TileContext ctx, Policy p) {
        // default: best-effort síncrono; providers específicos podem otimizar
        for (TileKey k : range) { fetch(k, ctx, p); }
    }
}