package com.marcusprado02.sharedkernel.contracts.api;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Contexto tipado e thread-safe (project Loom/virtual threads friendly). */
public final class ContextBag {
    private final Map<CtxKey<?>, Object> map = new ConcurrentHashMap<>();
    public <T> void put(CtxKey<T> key, T value) { map.put(key, key.type().cast(value)); }
    public <T> Optional<T> get(CtxKey<T> key) { return Optional.ofNullable(key.type().cast(map.get(key))); }
    public Map<String, Object> toDebugMap() {
        Map<String,Object> dbg = new LinkedHashMap<>();
        map.forEach((k,v) -> dbg.put(k.name(), v));
        return dbg;
    }
}