package com.marcusprado02.sharedkernel.infrastructure.maptile.api;

import com.marcusprado02.sharedkernel.infrastructure.maptile.model.TileContext;

public interface AttributionPolicy {
    void validate(String providerId, TileContext ctx) throws AttributionViolationException;
}