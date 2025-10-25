package com.marcusprado02.sharedkernel.crosscutting.aop.aspects;

import com.marcusprado02.sharedkernel.crosscutting.aop.adapter.spring.SpringInvocation;
import com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Idempotent;
import com.marcusprado02.sharedkernel.crosscutting.aop.core.AspectBase;
import com.marcusprado02.sharedkernel.crosscutting.aop.core.Invocation;
import com.marcusprado02.sharedkernel.crosscutting.idempotency.IdempotencyStore;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.HexFormat;
import java.util.Optional;

@Aspect
public class IdempotencyAspect extends AspectBase {

    private final IdempotencyStore store;

    public IdempotencyAspect(Tracer tracer, Meter meter, Clock clock, boolean failOpen,
                             IdempotencyStore store) {
        super(tracer, meter, clock, failOpen);
        this.store = store;
    }

    @Around("@within(com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Idempotent) || " +
            "@annotation(com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Idempotent)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        return super.invoke(new SpringInvocation(pjp));
    }

    @Override
    public Object invoke(Invocation inv) throws Throwable {
        var ann = inv.findAnnotation(Idempotent.class).orElseThrow();

        // 1) Chave: usa a fornecida ou gera hash(args, class, method)
        String key = !ann.key().isBlank()
                ? ann.key()
                : hashArgs(inv.getArguments(), inv.getClassName(), inv.getMethodName());

        // 2) Cache hit → retorna imediatamente
        Optional<Object> cached = store.tryGet(key);
        if (cached.isPresent()) return cached.get();

        // 3) Executa e guarda o resultado
        Object result = inv.proceed();
        store.put(key, result, ann.ttlSeconds());
        return result;
    }

    private String hashArgs(Object[] args, String cls, String m) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((cls + "#" + m).getBytes(StandardCharsets.UTF_8));
            if (args != null) {
                for (Object a : args) {
                    md.update(String.valueOf(a).getBytes(StandardCharsets.UTF_8));
                }
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            // fallback improvável; ainda assim, gera chave simples
            return (cls + "#" + m).hashCode() + ":" + (args == null ? 0 : args.length);
        }
    }
}
