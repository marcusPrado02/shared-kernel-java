package com.marcusprado02.sharedkernel.observability.metrics.bind;

import org.slf4j.MDC;
import java.util.*;

public final class MdcTagEnricher implements TagEnricher {
    private final String[] keys;
    public MdcTagEnricher(String... keys){ this.keys = keys==null? new String[0] : keys; }

    @Override public Map<String,String> enrich(Map<String,String> base) {
        Map<String,String> out = new LinkedHashMap<>(base==null? Map.of(): base);
        for (String k : keys) {
            String v = MDC.get(k);
            if (v != null && !v.isBlank()) out.put(k, v);
        }
        return out;
    }
}