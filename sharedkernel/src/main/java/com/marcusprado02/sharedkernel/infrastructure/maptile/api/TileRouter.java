package com.marcusprado02.sharedkernel.infrastructure.maptile.api;

import com.marcusprado02.sharedkernel.infrastructure.maptile.model.TileContext;

public interface TileRouter {
    /** Ex.: por país/zoom/tipo (base, satellite, terrain) seleciona provider. */
    String route(TileContext ctx);
}

