package com.marcusprado02.sharedkernel.infrastructure.maptile.spi;


import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.maptile.api.*;
import com.marcusprado02.sharedkernel.infrastructure.maptile.model.*;

public interface ProviderAdapter {
    ProviderMetadata metadata();

    TileData fetch(TileKey key, TileContext ctx, Policy policy);

    /** Opcional: provider já entrega raster estilizado (ex.: Satellite). */
    default RasterTile render(TileKey key, StyleRef style, TileContext ctx, Policy policy) {
        // default: gateway decodifica/estiliza; provider pode sobrepor.
        throw new UnsupportedOperationException("render not supported");
    }

    CompletableFuture<TileData> fetchAsync(TileKey key, TileContext ctx, Policy policy);

    /** Prefetch "best-effort" (pode usar bulk/pipeline). */
    void prefetch(TileRange range, TileContext ctx, Policy policy);
}
