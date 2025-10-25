package com.marcusprado02.sharedkernel.crosscutting.policies.core;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.marcusprado02.sharedkernel.crosscutting.policies.obligation.ObligationApplier;

@Aspect
public class PolicyAspect {
    private final PolicyEngine engine;
    private final ObligationApplier applier;

    public PolicyAspect(PolicyEngine engine, ObligationApplier applier) { this.engine = engine; this.applier = applier; }

    @Around("@annotation(pol)")
    public Object around(ProceedingJoinPoint pjp, Policy pol) throws Throwable {
        var ctx = extractContext(pjp, pol);
        var d = engine.decide(ctx.subject(), pol.action(), ctx.resource(), ctx.env());
        if (!d.isAllow()) throw new PolicyDeniedException(d);
        Object out = pjp.proceed();
        return applier.apply(out, d.obligations()); // ex.: mascarar campos no retorno
    }

    private CallContext extractContext(ProceedingJoinPoint pjp, Policy pol){
        // recuperar Subject (do SecurityContext), Resource (type/ID via args), Env (tenant/ip/clock)
        return CallContext.from(pjp, pol);
    }
}

