package com.marcusprado02.sharedkernel.infrastructure.search;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SearchAdapterRegistry<T> {
    private final Map<String, SearchAdapter<T>> byName = new ConcurrentHashMap<>();

    public void register(String name, SearchAdapter<T> adapter) { byName.put(name, adapter); }
    public SearchAdapter<T> get(String name) {
        var a = byName.get(name);
        if (a == null) throw new IllegalArgumentException("Adapter not found: " + name);
        return a;
    }
}
