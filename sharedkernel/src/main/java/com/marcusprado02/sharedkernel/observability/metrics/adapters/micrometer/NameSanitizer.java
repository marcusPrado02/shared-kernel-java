package com.marcusprado02.sharedkernel.observability.metrics.adapters.micrometer;

public interface NameSanitizer {
    String metricName(String raw);
    String tagKey(String raw);
    String tagValue(String raw);

    static NameSanitizer prometheusSafe() {
        return new NameSanitizer() {
            private String norm(String s){ return s==null? "" : s.trim().toLowerCase().replaceAll("[^a-z0-9:_]", "_"); }
            @Override public String metricName(String raw){ return norm(raw); }
            @Override public String tagKey(String raw){ return norm(raw); }
            @Override public String tagValue(String raw){ return raw==null? "" : raw.trim(); }
        };
    }
}