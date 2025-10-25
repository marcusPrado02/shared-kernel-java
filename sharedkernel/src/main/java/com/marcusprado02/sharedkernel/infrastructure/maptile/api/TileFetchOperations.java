package com.marcusprado02.sharedkernel.infrastructure.maptile.api;

import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.maptile.model.*;

public interface TileFetchOperations {
    TileData getTile(TileKey key, TileContext ctx, Policy policy);
    CompletableFuture<TileData> getTileAsync(TileKey key, TileContext ctx, Policy policy);
}
