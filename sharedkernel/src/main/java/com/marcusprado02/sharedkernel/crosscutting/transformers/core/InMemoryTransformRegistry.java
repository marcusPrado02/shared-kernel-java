package com.marcusprado02.sharedkernel.crosscutting.transformers.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryTransformRegistry implements TransformRegistry {
    private final Map<String, TransformFunction<?,?>> map = new ConcurrentHashMap<>();
    @Override public <I,O> void register(String n, String v, Class<I> i, Class<O> o, TransformFunction<I,O> fn) {
        map.put(key(n,v,i,o), fn);
    }
    @SuppressWarnings("unchecked")
    @Override public <I,O> TransformFunction<I,O> resolve(String n, String v, Class<I> i, Class<O> o) {
        var f = (TransformFunction<I,O>) map.get(key(n,v,i,o));
        if (f == null) throw new IllegalArgumentException("transform not found: " + n + "@" + v);
        return f;
    }
    private String key(String n, String v, Class<?> i, Class<?> o) { return n + "@" + v + "::" + i.getName() + "->" + o.getName(); }
}