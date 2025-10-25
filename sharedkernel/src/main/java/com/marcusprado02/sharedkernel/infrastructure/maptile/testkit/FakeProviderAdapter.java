package com.marcusprado02.sharedkernel.infrastructure.maptile.testkit;

import java.util.Set;

import com.marcusprado02.sharedkernel.infrastructure.maptile.api.*;
import com.marcusprado02.sharedkernel.infrastructure.maptile.core.BaseProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.maptile.model.*;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.*;

public class FakeProviderAdapter extends BaseProviderAdapter {
    @Override public ProviderMetadata metadata(){ return new ProviderMetadata("fake-tiles","Fake Tiles","1.0",
            Set.of(TileFormat.WEBP, TileFormat.PBF), Set.of("base"), Set.of("GLOBAL"),
            new Capabilities(true,true,true,true,true)); }
    @Override public TileData fetch(TileKey key, TileContext ctx, Policy p){
        // Simula latência/erros por tag
        if ("fail".equals(ctx.tags().getOrDefault("mode","ok"))) throw new RuntimeException("Simulated");
        var meta = new TileMeta("fake-tiles", new EtagInfo("W/\"etag-"+key.x()+"\"", null), 600, System.currentTimeMillis());
        byte[] bytes = new byte[]{ /* imagem dummy ou PBF dummy */ };
        return key.format()==TileFormat.PBF ? new VectorTile(bytes, meta) : new RasterTile(bytes, "image/webp", meta);
    }
}
