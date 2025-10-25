package com.marcusprado02.sharedkernel.crosscutting.policies.core;

import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.policies.cache.DecisionCache;

public final class CachingPolicyEngine implements PolicyEngine {
    private final PolicyEvaluator evaluator;          // OPA/Rego ou in-memory
    private final DecisionCache cache;
    private final DecisionLogger logger;
    private final boolean denyByDefault;

    public CachingPolicyEngine(PolicyEvaluator evaluator, DecisionCache cache,
                               DecisionLogger logger, boolean denyByDefault) {
        this.evaluator = evaluator; this.cache = cache; this.logger = logger; this.denyByDefault = denyByDefault;
    }

    @Override
    public Decision decide(Subject sub, String action, Resource res, Environment env) {
        String key = cache.key(env.tenant(), sub, action, res, env);
        return cache.get(key).orElseGet(() -> {
            Decision d;
            try {
                d = evaluator.evaluate(sub, action, res, env);
                if (d == null) d = deny("NO_POLICY_MATCH", "No policy matched", env);
            } catch (Exception e) {
                d = denyByDefault ? deny("ENGINE_ERROR", "Policy engine error", env)
                                  : allow("ENGINE_ERROR", "Fail-open", env);
            }
            cache.put(key, d, ttlFor(d));
            logger.logDecision(d, sub, action, res, env);
            return d;
        });
    }

    private Decision deny(String id, String reason, Environment env) {
        return new Decision(Effect.DENY, id, reason, Map.of(), currentTraceId());
    }
    private Decision allow(String id, String reason, Environment env) {
        return new Decision(Effect.ALLOW, id, reason, Map.of(), currentTraceId());
    }
    private long ttlFor(Decision d){ return d.effect()==Effect.ALLOW ? 60 : 10; }
    private String currentTraceId() {
        return io.opentelemetry.api.trace.Span.current().getSpanContext().getTraceId();
    }
}

