package com.marcusprado02.sharedkernel.observability.chaos;


import java.time.Duration;
import java.util.*;

public final class ChaosPolicy {
    public final String id;                 // estável (ex.: "orders-latency-p95")
    public final ChaosAction action;
    public final List<ChaosCondition> conditions;
    public final double maxBlastRadius;     // 0..1 (fração de requests elegíveis)
    public final Duration ttl;              // tempo máximo ativo (opcional)
    public final boolean requireAuthToken;  // exige header/token
    public final boolean enabled;

    public ChaosPolicy(String id, ChaosAction action, List<ChaosCondition> conditions,
                       double maxBlastRadius, Duration ttl, boolean requireAuthToken, boolean enabled){
        this.id = id; this.action = action; this.conditions = List.copyOf(conditions);
        this.maxBlastRadius = Math.max(0, Math.min(1, maxBlastRadius));
        this.ttl = ttl; this.requireAuthToken = requireAuthToken; this.enabled = enabled;
    }

    public static Builder builder(String id, ChaosAction action){ return new Builder(id, action); }
    public static final class Builder {
        private final String id; private final ChaosAction action;
        private final List<ChaosCondition> conds = new ArrayList<>();
        private double radius = 0.05; private java.time.Duration ttl;
        private boolean token=false; private boolean enabled=true;
        private Builder(String id, ChaosAction action){ this.id=id; this.action=action; }
        public Builder when(ChaosCondition c){ conds.add(c); return this; }
        public Builder blastRadius(double r){ this.radius=r; return this; }
        public Builder ttl(java.time.Duration t){ this.ttl=t; return this; }
        public Builder requireToken(boolean b){ this.token=b; return this; }
        public Builder enabled(boolean e){ this.enabled=e; return this; }
        public ChaosPolicy build(){ return new ChaosPolicy(id, action, conds, radius, ttl, token, enabled); }
    }
}
