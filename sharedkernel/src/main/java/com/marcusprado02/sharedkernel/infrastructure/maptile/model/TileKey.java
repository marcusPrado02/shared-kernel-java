package com.marcusprado02.sharedkernel.infrastructure.maptile.model;

import java.util.Map;

public record TileKey(int z, int x, int y, TileFormat format, boolean retina) {
    public String cacheKey(String styleId){
        return "%d/%d/%d/%s/%s@%s".formatted(z,x,y,format.name(), styleId == null ? "-" : styleId, retina ? "2x":"1x");
    }
}
