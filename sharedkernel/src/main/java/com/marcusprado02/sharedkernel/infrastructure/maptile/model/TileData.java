package com.marcusprado02.sharedkernel.infrastructure.maptile.model;

public sealed interface TileData permits RasterTile, VectorTile {
    TileMeta meta();
}