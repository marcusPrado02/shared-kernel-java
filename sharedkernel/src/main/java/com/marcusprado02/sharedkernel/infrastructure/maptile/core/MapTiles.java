package com.marcusprado02.sharedkernel.infrastructure.maptile.core;

import com.marcusprado02.sharedkernel.infrastructure.maptile.model.*;

import java.util.function.Function;

/** Façade leve para desacoplar a lib real de render/decoding do core. */
public final class MapTiles {
    private MapTiles(){}

    public static final class VectorTileDecoder {
        public static DecodedVector decode(byte[] pbf) {
            return new DecodedVector(pbf);
        }
        public record DecodedVector(byte[] pbf) {}
    }

    public static final class StyleEngine {
        /** Rasteriza vetor decodificado; stub de demonstração. */
        public static Image render(VectorTileDecoder.DecodedVector decoded, Object style, TileKey key, TileContext ctx) {
            byte[] content = decoded.pbf();
            return new Image(content, "image/webp");
        }
        public record Image(byte[] bytes, String contentType) {}
    }

    public static final class OfflinePackService {
        /**
         * Constrói um pacote offline somando bytes dos tiles.
         * O fetcher deve devolver o TileData (Raster ou Vector) para cada TileKey.
         */
        public static OfflinePackResult build(String packId, TileRange range, Function<TileKey, TileData> fetcher) {
            int count = 0;
            long total = 0;

            // usa topLeft()/bottomRight() recém adicionados
            for (int z = range.topLeft().z(); z <= range.bottomRight().z(); z++) {
                for (int x = range.topLeft().x(); x <= range.bottomRight().x(); x++) {
                    for (int y = range.topLeft().y(); y <= range.bottomRight().y(); y++) {
                        TileKey key = new TileKey(z, x, y, range.format(), range.retina());
                        TileData tile = fetcher.apply(key);
                        count++;

                        byte[] bytes;
                        if (tile instanceof RasterTile rt) {
                            bytes = rt.bytes();
                        } else if (tile instanceof VectorTile vt) {
                            bytes = vt.pbf();
                        } else {
                            bytes = null;
                        }
                        total += (bytes == null ? 0 : bytes.length);
                    }
                }
            }
            return new OfflinePackResult(packId, count, total, java.util.Map.of());
        }
    }
}