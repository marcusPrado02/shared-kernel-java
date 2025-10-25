package com.marcusprado02.sharedkernel.observability.chaos;


import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.marcusprado02.sharedkernel.observability.logging.structured.StructuredLogger;
import com.marcusprado02.sharedkernel.observability.metrics.core.MetricId;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;
import com.marcusprado02.sharedkernel.observability.tracing.SpanConfig;
import com.marcusprado02.sharedkernel.observability.tracing.TracingFacade;

public final class ChaosEngineImpl implements ChaosEngine {
    private final Map<String, ChaosPolicy> policies = new ConcurrentHashMap<>();
    private volatile boolean enabled = Boolean.parseBoolean(System.getenv().getOrDefault("CHAOS_ENABLED","false"));

    private final MetricsFacade metrics;
    private final StructuredLogger slog;
    private final TracingFacade tracing;
    private final String authToken; // header/secret opcional

    public ChaosEngineImpl(MetricsFacade metrics, StructuredLogger slog, TracingFacade tracing, String authToken){
        this.metrics = metrics; this.slog = slog; this.tracing = tracing; this.authToken = authToken;
    }

    @Override public boolean maybeInject(ChaosContext ctx) throws Exception {
        if (!enabled) return false;

        boolean injected = false;
        for (ChaosPolicy p : policies.values()) {
            if (!p.enabled) continue;

            // TTL guardrail
            // (supondo que TTL será checado por quem registra a policy; aqui simples)
            double pEff = effectiveProbability(p, ctx);
            if (pEff <= 0) continue;

            if (ThreadLocalRandom.current().nextDouble() < pEff) {
                // tracing + auditoria
                var spanCfg = SpanConfig.builder("chaos.apply")
                        .attr("chaos.policy", p.id).attr("chaos.action", p.action.name())
                        .attr("route", ctx.route).attr("tenant", ctx.tenant)
                        .build();
                try (var span = tracing.startSpan(spanCfg)) {
                    long t0 = System.nanoTime();
                    p.action.apply(ctx);
                    long dt = (System.nanoTime()-t0)/1_000_000;
                    metrics.increment(MetricId.builder("chaos","applied").build(), 1, Map.of("action", p.action.name()));
                    slog.info("chaos.applied: policy=" + p.id +
                              ", action=" + p.action.name() +
                              ", route=" + ctx.route +
                              ", tenant=" + ctx.tenant +
                              ", duration_ms=" + dt);
                }
                injected = true;
            }
        }
        return injected;
    }

    private double effectiveProbability(ChaosPolicy p, ChaosContext ctx){
        // autorização se necessário
        if (p.requireAuthToken) {
            // Authorization soft-check: o token é propagado para tags no ctx (ex.: via Filter)
            String tok = ctx.tags==null? null : ctx.tags.get("chaos.token");
            if (tok == null || !Objects.equals(tok, authToken)) return 0.0;
        }
        // composição: prob_final = min(blastRadius, média das probs>0 das condições)
        double sum = 0; int n = 0;
        for (var c : p.conditions) {
            double pc = c.probability(ctx);
            if (pc > 0) { sum += pc; n++; }
        }
        if (n == 0) return 0.0;
        double avg = sum / n;
        return Math.min(p.maxBlastRadius, avg);
    }

    @Override public List<ChaosPolicy> policies(){ return List.copyOf(policies.values()); }
    @Override public void addPolicy(ChaosPolicy p){ policies.put(p.id, p); }
    @Override public void removePolicy(String id){ policies.remove(id); }
    @Override public void clear(){ policies.clear(); }
    @Override public void setEnabled(boolean e){ this.enabled = e; }
    @Override public boolean isEnabled(){ return enabled; }
}