package com.marcusprado02.sharedkernel.infrastructure.maptile.model;

import java.util.Map;

public record TileContext(
        String tenantId, String usage, String layer, Projection projection,
        String preferredProvider, Map<String,Object> tags
) {}
