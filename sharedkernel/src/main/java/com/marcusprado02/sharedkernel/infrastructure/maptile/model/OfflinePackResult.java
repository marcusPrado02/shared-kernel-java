package com.marcusprado02.sharedkernel.infrastructure.maptile.model;

import java.util.Map;

public record OfflinePackResult(
        String packId,
        int tilesCount,
        long bytes,
        Map<String,Object> meta
) {}
