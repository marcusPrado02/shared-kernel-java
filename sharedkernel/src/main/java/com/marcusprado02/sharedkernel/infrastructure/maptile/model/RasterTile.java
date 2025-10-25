package com.marcusprado02.sharedkernel.infrastructure.maptile.model;

public record RasterTile(byte[] bytes, String mime, TileMeta meta) implements TileData {}

