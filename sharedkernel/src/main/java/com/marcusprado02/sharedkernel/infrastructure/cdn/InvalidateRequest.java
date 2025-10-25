package com.marcusprado02.sharedkernel.infrastructure.cdn;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record InvalidateRequest(
        String distributionId,             // id/distribution/zone/service (depende do provedor)
        List<Target> targets,
        Mode mode,                         // HARD/SOFT
        TTLOverride ttlOverride,           // apenas relevante no SOFT
        boolean waitForCompletion,         // bloqueia até "Completed"
        Scope scope,                       // nem todas CDNs suportam
        String idempotencyKey,             // para deduplicar
        Map<String, String> headers,       // headers extras (Akamai/CF X-*)
        Map<String, String> attributes     // metadados (tenant, actor, reason, changeId)
) {
    public static Builder forDist(String distributionId){ return new Builder(distributionId); }
    public static final class Builder {
        private final String distributionId;
        private final List<Target> targets = new ArrayList<>();
        private Mode mode = Mode.HARD;
        private TTLOverride ttlOverride = TTLOverride.none();
        private boolean waitForCompletion = false;
        private Scope scope = Scope.GLOBAL;
        private String idempotencyKey = UUID.randomUUID().toString();
        private Map<String,String> headers = new HashMap<>();
        private Map<String,String> attributes = new HashMap<>();

        private Builder(String distributionId){ this.distributionId = distributionId; }
        public Builder add(Target t){ this.targets.add(t); return this; }
        public Builder hard(){ this.mode = Mode.HARD; return this; }
        public Builder soft(Duration swr, Duration sie){ this.mode = Mode.SOFT; this.ttlOverride = new TTLOverride(swr, sie); return this; }
        public Builder waitDone(){ this.waitForCompletion = true; return this; }
        public Builder scope(Scope s){ this.scope = s; return this; }
        public Builder idempotency(String k){ this.idempotencyKey = k; return this; }
        public Builder header(String k,String v){ this.headers.put(k,v); return this; }
        public Builder attribute(String k,String v){ this.attributes.put(k,v); return this; }
        public InvalidateRequest build(){
            if (targets.isEmpty()) throw new IllegalArgumentException("targets required");
            return new InvalidateRequest(distributionId, List.copyOf(targets), mode, ttlOverride, waitForCompletion, scope, idempotencyKey, Map.copyOf(headers), Map.copyOf(attributes));
        }
    }
}
