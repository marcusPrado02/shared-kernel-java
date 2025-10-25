package com.marcusprado02.sharedkernel.crosscutting.cache;


import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCacheStore implements CacheStore {
    private static final class Entry {
        final Object value; final long expiresAt;
        Entry(Object v, long exp){ this.value = v; this.expiresAt = exp; }
    }
    private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();

    @Override
    public Object get(String key) {
        Entry e = map.get(key);
        if (e == null) return null;
        if (e.expiresAt > 0 && e.expiresAt < System.currentTimeMillis()) {
            map.remove(key);
            return null;
        }
        return e.value;
    }

    @Override
    public void set(String key, Object value, long ttlSeconds) {
        if (ttlSeconds <= 0) return; // sem cache
        long exp = System.currentTimeMillis() + ttlSeconds * 1000L;
        map.put(key, new Entry(value, exp));
    }
}