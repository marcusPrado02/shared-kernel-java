package com.marcusprado02.sharedkernel.crosscutting.aop.aspects;

import com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Encrypted;
import com.marcusprado02.sharedkernel.crosscutting.aop.core.AspectBase;
import com.marcusprado02.sharedkernel.crosscutting.aop.core.Invocation;
import com.marcusprado02.sharedkernel.crosscutting.aop.adapter.spring.SpringInvocation;
import com.marcusprado02.sharedkernel.crypto.CipherText;
import com.marcusprado02.sharedkernel.crypto.CryptoService;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.time.Clock;

@Aspect
public class EncryptionAspect extends AspectBase {

    private final CryptoService crypto;

    public EncryptionAspect(Tracer tracer, Meter meter, Clock clock, boolean failOpen,
                            CryptoService crypto) {
        super(tracer, meter, clock, failOpen);
        this.crypto = crypto;
    }

    @Around("@within(com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Encrypted) || " +
            "@annotation(com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Encrypted)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        return super.invoke(new SpringInvocation(pjp));
    }

    @Override
    public Object invoke(Invocation inv) throws Throwable {
        var ann = inv.findAnnotation(Encrypted.class).orElseThrow();

        // Pré: decriptar argumentos in-place, quando marcado
        if (ann.decryptArgs()) {
            decryptArgsInPlace(inv.getArguments(), ann.profile());
        }

        // Execução real
        Object result = inv.proceed();

        // Pós: criptografar retorno quando configurado e não nulo
        if (ann.encryptReturn() && result != null) {
            result = crypto.encryptObject(result, ann.profile());
        }
        return result;
    }

    private void decryptArgsInPlace(Object[] args, String profile) {
        if (args == null) return;
        for (int i = 0; i < args.length; i++) {
            Object v = args[i];
            if (v instanceof CipherText ct) {
                args[i] = crypto.decrypt(ct, profile);
            }
        }
    }
}
