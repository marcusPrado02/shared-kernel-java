package com.marcusprado02.sharedkernel.infrastructure.maptile.core;

import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.maptile.model.TileData;

public interface InflightDedupe {
    CompletableFuture<TileData> compute(String key, java.util.concurrent.Callable<TileData> loader);
}
