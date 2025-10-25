package com.marcusprado02.sharedkernel.infrastructure.maptile.model;

public record VectorTile(byte[] pbf, TileMeta meta) implements TileData {}

