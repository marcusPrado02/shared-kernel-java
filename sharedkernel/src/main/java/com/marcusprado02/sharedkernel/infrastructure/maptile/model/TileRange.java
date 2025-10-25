package com.marcusprado02.sharedkernel.infrastructure.maptile.model;


import java.util.Iterator;
import java.util.NoSuchElementException;

public final class TileRange implements Iterable<TileKey> {
    private final int zMin, zMax, xMin, xMax, yMin, yMax;
    private final TileFormat format;
    private final boolean retina;

    public TileRange(int zMin, int zMax, int xMin, int xMax, int yMin, int yMax, TileFormat format, boolean retina) {
        this.zMin=zMin; this.zMax=zMax; this.xMin=xMin; this.xMax=xMax; this.yMin=yMin; this.yMax=yMax;
        this.format=format; this.retina=retina;
    }

    public int zMin(){ return zMin; }
    public int zMax(){ return zMax; }
    public TileFormat format(){ return format; }
    public boolean retina(){ return retina; }

    /** Canto superior esquerdo do range. */
    public TileKey topLeft() {
        return new TileKey(zMin, xMin, yMin, format, retina);
    }

    /** Canto inferior direito do range. */
    public TileKey bottomRight() {
        return new TileKey(zMax, xMax, yMax, format, retina);
    }

    @Override public Iterator<TileKey> iterator() {
        return new Iterator<>() {
            int x = xMin, y = yMin;
            @Override public boolean hasNext() { return x <= xMax && y <= yMax; }
            @Override public TileKey next() {
                if (!hasNext()) throw new NoSuchElementException();
                TileKey k = new TileKey(zMin, x, y, format, retina);
                // avança linha
                if (x < xMax) x++; else { x = xMin; y++; }
                return k;
            }
        };
    }
}