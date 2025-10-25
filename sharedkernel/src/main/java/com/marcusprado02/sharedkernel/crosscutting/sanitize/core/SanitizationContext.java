package com.marcusprado02.sharedkernel.crosscutting.sanitize.core;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Flow.*;

public record SanitizationContext(
        String purpose,                 // ex.: "user-input.email"
        Locale locale,
        Map<String, Object> attributes  // ex.: tenantId, traceId, maxLength
) {
    public static Builder builder(){ return new Builder(); }
    public static final class Builder {
        private String purpose="default";
        private Locale locale=Locale.ROOT;
        private final Map<String,Object> attrs = new ConcurrentHashMap<>();
        public Builder purpose(String p){this.purpose=p;return this;}
        public Builder locale(Locale l){this.locale=l;return this;}
        public Builder attribute(String k,Object v){attrs.put(k,v);return this;}
        public SanitizationContext build(){ return new SanitizationContext(purpose, locale, Map.copyOf(attrs)); }
    }
}
