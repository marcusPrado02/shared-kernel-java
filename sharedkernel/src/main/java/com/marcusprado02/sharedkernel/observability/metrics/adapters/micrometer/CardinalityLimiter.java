package com.marcusprado02.sharedkernel.observability.metrics.adapters.micrometer;

import java.util.*;
import java.util.stream.Collectors;

/** Evita explosão de cardinalidade por má formação de tags (IDs únicos, etc.). */
public final class CardinalityLimiter {
    private final int maxKeys;
    private final int maxValueLen;
    private final Set<String> allowedKeys;
    private final Set<String> dropKeys;

    public CardinalityLimiter(int maxKeys, int maxValueLen, Set<String> allowedKeys, Set<String> dropKeys) {
        this.maxKeys = maxKeys;
        this.maxValueLen = maxValueLen;
        this.allowedKeys = allowedKeys==null? Set.of(): Set.copyOf(allowedKeys);
        this.dropKeys = dropKeys==null? Set.of(): Set.copyOf(dropKeys);
    }

    public Map<String,String> limit(Map<String,String> in){
        if (in==null || in.isEmpty()) return Map.of();
        Map<String,String> m = new LinkedHashMap<>();
        for (var e : in.entrySet()) {
            var k = e.getKey();
            if (dropKeys.contains(k)) continue;
            if (!allowedKeys.isEmpty() && !allowedKeys.contains(k)) continue;
            var v = e.getValue();
            if (v != null && v.length() > maxValueLen) v = v.substring(0, maxValueLen);
            m.put(k, v);
            if (m.size() >= maxKeys) break;
        }
        return m;
    }

    public static CardinalityLimiter permissive() {
        return new CardinalityLimiter(16, 128, Set.of(), Set.of());
    }
}