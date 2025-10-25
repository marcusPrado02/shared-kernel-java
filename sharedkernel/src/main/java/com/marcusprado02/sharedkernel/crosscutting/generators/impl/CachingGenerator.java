package com.marcusprado02.sharedkernel.crosscutting.generators.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.generators.core.*;

public final class CachingGenerator<T> implements Generator<T> {
    private final Generator<T> delegate;
    private final int capacity;
    private final Map<Integer,T> cache;

    public CachingGenerator(Generator<T> d, int capacity) {
        this.delegate=d; this.capacity=capacity;
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true){
            protected boolean removeEldestEntry(Map.Entry<Integer,T> e){ return size()>capacity; }
        };
    }

    @Override public synchronized T generate(GenerationContext ctx) {
        int key = ctx.hashCode();
        T v = cache.get(key);
        if (v!=null) return v;
        v = delegate.generate(ctx);
        cache.put(key, v);
        return v;
    }
}
