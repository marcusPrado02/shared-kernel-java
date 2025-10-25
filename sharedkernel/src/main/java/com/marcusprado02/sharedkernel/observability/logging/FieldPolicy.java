package com.marcusprado02.sharedkernel.observability.logging;

import java.util.*;

public final class FieldPolicy {
    public final int maxFields;      // limite total de campos (evita explosão)
    public final int maxValueLen;    // truncamento de strings
    public final Set<String> whitelist; // se não vazia → apenas essas keys
    public final Set<String> blacklist; // sempre removidas

    public FieldPolicy(int maxFields, int maxValueLen, Set<String> whitelist, Set<String> blacklist) {
        this.maxFields = maxFields; this.maxValueLen = maxValueLen;
        this.whitelist = whitelist==null? Set.of(): Set.copyOf(whitelist);
        this.blacklist = blacklist==null? Set.of(): Set.copyOf(blacklist);
    }

    public Map<String,Object> apply(Map<String,Object> in) {
        if (in == null || in.isEmpty()) return Map.of();
        Map<String,Object> out = new LinkedHashMap<>();
        for (var e : in.entrySet()) {
            String k = sanitizeKey(e.getKey());
            if (blacklist.contains(k)) continue;
            if (!whitelist.isEmpty() && !whitelist.contains(k)) continue;
            Object v = e.getValue();
            if (v instanceof String s && s.length() > maxValueLen) v = s.substring(0, maxValueLen) + "...";
            out.put(k, v);
            if (out.size() >= maxFields) break;
        }
        return out;
    }

    private String sanitizeKey(String k){
        if (k == null) return "";
        return k.trim().toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
    }

    public static FieldPolicy permissive() { return new FieldPolicy(64, 1024, Set.of(), Set.of()); }
}