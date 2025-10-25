package com.marcusprado02.sharedkernel.observability.chaos.adapter.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;

import com.marcusprado02.sharedkernel.observability.chaos.ChaosContext;
import com.marcusprado02.sharedkernel.observability.chaos.ChaosEngine;
import com.marcusprado02.sharedkernel.observability.chaos.annotation.Chaotic;

import java.util.Map;

@Aspect
public class ChaoticAspect {
    private final ChaosEngine engine;

    public ChaoticAspect(ChaosEngine engine){ this.engine = engine; }

    @Around("@annotation(c)")
    public Object around(ProceedingJoinPoint pjp, Chaotic c) throws Throwable {
        ChaosContext ctx = new ChaosContext(
                c.route().isBlank()? pjp.getSignature().toShortString() : c.route(),
                c.method(),
                MDC.get("tenant"), MDC.get("user"), MDC.get("trace_id"),
                Map.of()
        );
        engine.maybeInject(ctx);
        return pjp.proceed();
    }
}