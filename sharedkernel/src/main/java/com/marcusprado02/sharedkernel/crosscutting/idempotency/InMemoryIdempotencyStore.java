package com.marcusprado02.sharedkernel.crosscutting.idempotency;


import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryIdempotencyStore implements IdempotencyStore {
    private static final class Entry { final Object v; final long exp; Entry(Object v, long exp){this.v=v;this.exp=exp;} }
    private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();

    @Override public Optional<Object> tryGet(String key) {
        var e = map.get(key);
        if (e == null) return Optional.empty();
        if (e.exp < System.currentTimeMillis()) { map.remove(key); return Optional.empty(); }
        return Optional.ofNullable(e.v);
    }

    @Override public void put(String key, Object value, long ttlSeconds) {
        long exp = System.currentTimeMillis() + Math.max(1, ttlSeconds) * 1000L;
        map.put(key, new Entry(value, exp));
    }
}
