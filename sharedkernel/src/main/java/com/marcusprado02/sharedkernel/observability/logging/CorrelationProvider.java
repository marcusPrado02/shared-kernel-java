package com.marcusprado02.sharedkernel.observability.logging;


import org.slf4j.MDC;
import java.util.*;

public interface CorrelationProvider {
    Map<String,String> correlation();

    static CorrelationProvider mdcDefault() {
        return () -> {
            Map<String,String> m = new LinkedHashMap<>();
            putIfPresent(m, "trace_id", MDC.get("trace_id"));   // preenchido pelo TracingAspect/OTel bridge
            putIfPresent(m, "span_id", MDC.get("span_id"));
            putIfPresent(m, "tenant", MDC.get("tenant"));
            putIfPresent(m, "user", MDC.get("user"));
            putIfPresent(m, "req_id", MDC.get("requestId"));
            return m;
        };
    }
    private static void putIfPresent(Map<String,String> m, String k, String v){ if (v != null && !v.isBlank()) m.put(k, v); }
}
