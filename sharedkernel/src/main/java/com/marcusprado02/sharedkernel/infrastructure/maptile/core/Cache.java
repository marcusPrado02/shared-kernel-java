package com.marcusprado02.sharedkernel.infrastructure.maptile.core;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.maptile.model.TileData;

public interface Cache {
    Optional<TileData> get(String cacheKey);
    void put(String cacheKey, TileData data, long ttlSeconds);
    void invalidate(String cacheKey);
}
