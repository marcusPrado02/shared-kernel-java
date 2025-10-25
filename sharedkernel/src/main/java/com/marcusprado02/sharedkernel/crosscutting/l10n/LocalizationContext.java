package com.marcusprado02.sharedkernel.crosscutting.l10n;

import java.time.ZoneId;
import java.util.*;

public record LocalizationContext(
    Locale locale,
    ZoneId zoneId,
    Optional<String> tenant,               // "acme", "globex"...
    Map<String, Object> attributes         // ex. traceId, userId, currency override
) {
    public static Builder builder(){ return new Builder(); }
    public static final class Builder {
        private Locale locale = Locale.ROOT;
        private ZoneId zoneId = ZoneId.of("UTC");
        private Optional<String> tenant = Optional.empty();
        private final Map<String,Object> attrs = new HashMap<>();
        public Builder locale(Locale l){ this.locale=l; return this; }
        public Builder zoneId(ZoneId z){ this.zoneId=z; return this; }
        public Builder tenant(String t){ this.tenant=Optional.ofNullable(t); return this; }
        public Builder attribute(String k, Object v){ this.attrs.put(k,v); return this; }
        public LocalizationContext build(){ return new LocalizationContext(locale, zoneId, tenant, Map.copyOf(attrs)); }
    }
}
