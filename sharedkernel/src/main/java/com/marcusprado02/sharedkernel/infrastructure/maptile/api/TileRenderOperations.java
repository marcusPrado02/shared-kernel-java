package com.marcusprado02.sharedkernel.infrastructure.maptile.api;

import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.maptile.model.*;

public interface TileRenderOperations {
    /** Renderiza vetor -> raster conforme style; se já raster, retorna como está. */
    RasterTile render(TileKey key, StyleRef style, TileContext ctx, Policy policy);
    CompletableFuture<RasterTile> renderAsync(TileKey key, StyleRef style, TileContext ctx, Policy policy);
}