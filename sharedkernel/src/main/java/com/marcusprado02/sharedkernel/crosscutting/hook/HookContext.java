package com.marcusprado02.sharedkernel.crosscutting.hook;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;

/** Contexto padrão para hooks e callbacks. */
public record HookContext(
    String topic,                 // ex.: "order.created"
    String phase,                 // "before"/"after"/"around"/"error"
    Clock clock,
    Optional<UUID> correlationId, // para idempotência, tracing
    Map<String, Object> attributes,
    CancellationToken cancellation // cancelamento cooperativo (abaixo)
) {
    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private String topic="default";
        private String phase="invoke";
        private Clock clock=Clock.systemUTC();
        private Optional<UUID> corr=Optional.empty();
        private final Map<String,Object> attrs = new ConcurrentHashMap<>();
        private CancellationToken token = CancellationToken.NONE;
        public Builder topic(String t){this.topic=t;return this;}
        public Builder phase(String p){this.phase=p;return this;}
        public Builder clock(Clock c){this.clock=c;return this;}
        public Builder correlationId(UUID id){this.corr=Optional.ofNullable(id);return this;}
        public Builder attribute(String k, Object v){attrs.put(k,v);return this;}
        public Builder cancellation(CancellationToken t){this.token=t;return this;}
        public HookContext build(){ return new HookContext(topic, phase, clock, corr, Map.copyOf(attrs), token); }
    }
}

