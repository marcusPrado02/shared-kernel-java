package com.marcusprado02.sharedkernel.crosscutting.policies.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.marcusprado02.sharedkernel.crosscutting.hash.Hashing;
import com.marcusprado02.sharedkernel.crosscutting.hash.MessageDigestHolder;
import com.marcusprado02.sharedkernel.crosscutting.policies.core.*;

public final class InMemoryDecisionCache implements DecisionCache {
    private final ConcurrentHashMap<String, CacheEntry> map = new ConcurrentHashMap<>();
    record CacheEntry(Decision d, long exp) {}

    @Override
    public Optional<Decision> get(String key) {
        var ce = map.get(key);
        if (ce == null || ce.exp < System.currentTimeMillis()) return Optional.empty();
        return Optional.of(ce.d);
    }

    @Override
    public void put(String key, Decision d, long ttl) {
        map.put(key, new CacheEntry(d, System.currentTimeMillis() + ttl * 1000));
    }

    @Override
    public String key(String tenant, Subject s, String action, Resource r, Environment e) {
        // Atenção: não inclua PII; use hash estável de atributos
        String base = tenant + "|" + s.roles() + "|" + action + "|" + r.type() + "|" + String.valueOf(r.id())
                + "|" + attrsHash(s.attrs(), r.attrs(), e.attrs());
        return MessageDigestHolder.sha256Hex(base);
    }

    @SafeVarargs
    private final String attrsHash(Map<String, Object>... maps) {
        return Hashing.stableHash(maps);
    }
}
