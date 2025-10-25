package com.marcusprado02.sharedkernel.crosscutting.hash;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Hash estável e determinístico p/ mapas (sem PII). */
public final class Hashing {
    private Hashing() {}

    @SafeVarargs
    public static String stableHash(Map<String, Object>... maps) {
        List<String> parts = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> m : maps) {
                if (m == null) continue;
                m.keySet().stream().sorted().forEach(k -> {
                    Object v = m.get(k);
                    // Normaliza valores para string curta e estável
                    parts.add(k + "=" + normalizeValue(v));
                });
            }
        }
        return MessageDigestHolder.sha256Hex(String.join("|", parts));
    }

    private static String normalizeValue(Object v) {
        if (v == null) return "null";
        // Evite PII: não serialize objetos inteiros, só toString curto
        String s = String.valueOf(v);
        if (s.length() > 64) s = s.substring(0, 64);
        return s;
    }
}
