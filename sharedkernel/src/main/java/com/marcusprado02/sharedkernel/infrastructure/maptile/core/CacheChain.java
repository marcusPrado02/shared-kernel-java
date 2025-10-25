package com.marcusprado02.sharedkernel.infrastructure.maptile.core;

import java.util.Optional;

import com.marcusprado02.sharedkernel.infrastructure.maptile.model.TileData;

public final class CacheChain implements Cache {
    private final Cache[] chain;
    public CacheChain(Cache... chain){ this.chain = chain; }
    @Override public Optional<TileData> get(String k){
        Optional<TileData> v = Optional.empty();
        for (Cache c : chain) { v = c.get(k); if (v.isPresent()) break; }
        // read-through: reidrata níveis superiores (opcional)
        v.ifPresent(val -> { for (int i=0;i<chain.length-1;i++) chain[i].put(k, val, val.meta().ttlSeconds());});
        return v;
    }
    @Override public void put(String k, TileData d, long ttl){ for (Cache c : chain) c.put(k,d,ttl); }
    @Override public void invalidate(String k){ for (Cache c : chain) c.invalidate(k); }
}
