package com.marcusprado02.sharedkernel.infrastructure.maptile.adapter.mbtiles;

import java.util.Set;

import com.marcusprado02.sharedkernel.infrastructure.maptile.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.maptile.model.*;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.Capabilities;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.ProviderMetadata;
import com.marcusprado02.sharedkernel.infrastructure.maptile.spi.ProviderConfig;
import com.marcusprado02.sharedkernel.infrastructure.maptile.core.BaseProviderAdapter;

public class MBTilesProviderAdapter extends BaseProviderAdapter {

    private final javax.sql.DataSource ds;
    private final String id = "mbtiles-local";

    public MBTilesProviderAdapter(ProviderConfig cfg, javax.sql.DataSource ds){ this.ds = ds; }

    @Override public ProviderMetadata metadata() {
        return new ProviderMetadata(id, "MBTiles Local", "1.0",
                Set.of(TileFormat.PNG, TileFormat.JPEG, TileFormat.WEBP, TileFormat.PBF),
                Set.of("base"), Set.of("LOCAL"),
                new Capabilities(true,true,true,false,true));
    }

    @Override public TileData fetch(TileKey key, TileContext ctx, Policy p) {
        return run(p, () -> {
            try (var c = ds.getConnection();
                 var ps = c.prepareStatement("SELECT tile_data FROM tiles WHERE zoom_level=? AND tile_column=? AND tile_row=?")) {
                ps.setInt(1, key.z()); ps.setInt(2, key.x()); ps.setInt(3, tmsToMbtilesY(key.z(), key.y()));
                var rs = ps.executeQuery();
                if (!rs.next()) throw new RuntimeException("Tile not found");
                byte[] bytes = rs.getBytes(1);
                TileMeta meta = new TileMeta(id, new EtagInfo(null,null), 86400, System.currentTimeMillis());
                if (key.format() == TileFormat.PBF) return new VectorTile(bytes, meta);
                return new RasterTile(bytes, detectMime(bytes), meta);
            } catch (Exception e){ throw new RuntimeException(e); }
        });
    }

    private int tmsToMbtilesY(int z, int y){ return ((1 << z) - 1) - y; }
    private String detectMime(byte[] b){ return "image/webp"; }
}

