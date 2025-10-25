package com.marcusprado02.sharedkernel.infrastructure.maptile.spi;

public record Capabilities(
        boolean supportsVector,
        boolean supportsRaster,
        boolean supportsRetina,
        boolean supportsETag,
        boolean supportsGzip
) {}