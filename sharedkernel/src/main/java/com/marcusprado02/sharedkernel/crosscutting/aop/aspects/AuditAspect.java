package com.marcusprado02.sharedkernel.crosscutting.aop.aspects;

import com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Audited;
import com.marcusprado02.sharedkernel.crosscutting.aop.core.AspectBase;
import com.marcusprado02.sharedkernel.crosscutting.aop.core.Invocation;
import com.marcusprado02.sharedkernel.crosscutting.aop.adapter.spring.SpringInvocation;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;

@Aspect
public class AuditAspect extends AspectBase {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    public AuditAspect(Tracer tracer, Meter meter, Clock clock, boolean failOpen) {
        super(tracer, meter, clock, failOpen);
    }

    @Around("@within(com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Audited) || " +
            "@annotation(com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Audited)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        return super.invoke(new SpringInvocation(pjp));
    }

    @Override
    protected void before(Invocation inv, Span span) {
        inv.findAnnotation(Audited.class).ifPresent(a -> {
            span.setAttribute("audit.action", a.action());
        });
    }

    @Override
    protected void after(Invocation inv, Object result, Span span, Instant start) {
        super.after(inv, result, span, start);

        var ann = inv.findAnnotation(Audited.class);
        if (ann.isPresent()) {
            boolean includeArgs = ann.get().includeArgs();
            log.info("AUDIT class={} method={} action={} ok=true args={}",
                    inv.getClassName(),
                    inv.getMethodName(),
                    ann.get().action(),
                    includeArgs ? safeArgs(inv.getArguments()) : "[redacted]");
        }
    }

    private Object safeArgs(Object[] args) {
        // Redija PII; evite toString custoso
        return Arrays.stream(args).map(this::safeValue).toList();
    }

    private Object safeValue(Object v) {
        if (v == null) return null;
        String s = v.toString();
        if (s.length() > 200) s = s.substring(0, 200) + "...";
        return s;
    }
}
