package com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.comp;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.*;

public final class CompositeLimiter implements RateLimiter {
    private final RateLimiter local;
    private final RateLimiter distributed;
    private final ConcurrentHashMap<String, AtomicInteger> violations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> penaltyUntil = new ConcurrentHashMap<>();

    public CompositeLimiter(RateLimiter local, RateLimiter distributed){
        this.local = local; this.distributed = distributed;
    }

    @Override public Decision evaluateAndConsume(RateKey key, LimitSpec spec) {
        String k = key.asString()+"|"+spec.name();
        long nowSec = Instant.now().getEpochSecond();
        var until = penaltyUntil.getOrDefault(k, 0L);
        if (until > nowSec) {
            return new Decision(false, 0, until, Map.of("penalty", true));
        }

        // 1) bucket local rápido
        var localDec = local.evaluateAndConsume(key, spec);
        if (!localDec.allowed()) {
            bump(k, spec);
            return blockOrShadow(localDec, spec, Map.of("stage","local"));
        }

        // 2) janela distribuída
        var distDec = distributed.evaluateAndConsume(key, spec);
        if (!distDec.allowed()) {
            bump(k, spec);
            return blockOrShadow(distDec, spec, Map.of("stage","distributed"));
        }

        // success → zera contador de violações
        violations.remove(k);
        return new Decision(true, distDec.remaining(), distDec.resetEpochSeconds(), Map.of("ok", true));
    }

    private Decision blockOrShadow(Decision base, LimitSpec spec, Map<String,Object> extra){
        if (!spec.blockOnExceed()) {
            return new Decision(true, Math.max(0, base.remaining()), base.resetEpochSeconds(), merge(extra, Map.of("shadow", true)));
        }
        return new Decision(false, 0, base.resetEpochSeconds(), extra);
    }

    private void bump(String k, LimitSpec spec){
        int v = violations.computeIfAbsent(k, key -> new AtomicInteger()).incrementAndGet();
        if (spec.penalty()!=null && spec.penaltyThreshold() > 0 && v >= spec.penaltyThreshold()) {
            long until = Instant.now().plus(spec.penalty()).getEpochSecond();
            penaltyUntil.put(k, until);
            violations.remove(k);
        }
    }
    private static Map<String,Object> merge(Map<String,Object> a, Map<String,Object> b){
        var m = new java.util.HashMap<String,Object>(a); m.putAll(b); return m;
    }
}
