package com.marcusprado02.sharedkernel.infrastructure.maptile.api;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.maptile.model.*;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.*;

public interface MapTileGateway extends TileFetchOperations, TileRenderOperations {
    Optional<String> resolveProvider(TileContext ctx);
    Capabilities capabilities(String providerId);

    /** Prefetch de um range (p/ offline/heat cache). */
    void prefetch(TileRange range, TileContext ctx, Policy policy);

    /** Cria/atualiza pacote offline (MBTiles). */
    OfflinePackResult buildOfflinePack(String packId, TileRange range, TileContext ctx);

    /** Limpa caches seletivamente. */
    void invalidate(TileKey key, TileContext ctx);
}
