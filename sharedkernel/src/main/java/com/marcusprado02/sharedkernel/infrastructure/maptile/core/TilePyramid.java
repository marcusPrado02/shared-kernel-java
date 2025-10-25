package com.marcusprado02.sharedkernel.infrastructure.maptile.core;

import com.marcusprado02.sharedkernel.infrastructure.maptile.model.LatLng;

public final class TilePyramid {
    private static final double PI = Math.PI;
    public static long quadKey(int z, int x, int y){
        StringBuilder sb = new StringBuilder();
        for (int i=z; i>0; i--){
            int digit = 0;
            int mask = 1 << (i-1);
            if ((x & mask) != 0) digit++;
            if ((y & mask) != 0) digit += 2;
            sb.append(digit);
        }
        return Long.parseUnsignedLong(sb.toString());
    }
    public static LatLng tileToLatLng(int z, int x, int y){
        double n = Math.pow(2.0, z);
        double lon = x / n * 360.0 - 180.0;
        double latRad = Math.atan(Math.sinh(PI * (1 - 2 * y / n)));
        double lat = Math.toDegrees(latRad);
        return new LatLng(lat, lon);
    }
}
