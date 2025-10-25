package com.marcusprado02.sharedkernel.infrastructure.maptile.spi;

import java.util.Set;
import com.marcusprado02.sharedkernel.infrastructure.maptile.model.TileFormat;

public record ProviderMetadata(
        String id,
        String displayName,
        String version,
        Set<TileFormat> supportedFormats,
        Set<String> supportedLayers,
        Set<String> regions,
        Capabilities capabilities
) {}