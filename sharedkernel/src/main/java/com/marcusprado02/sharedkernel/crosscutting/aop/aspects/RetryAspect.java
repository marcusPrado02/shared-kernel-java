package com.marcusprado02.sharedkernel.crosscutting.aop.aspects;

import com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Retryable;
import com.marcusprado02.sharedkernel.crosscutting.aop.core.AspectBase;
import com.marcusprado02.sharedkernel.crosscutting.aop.core.Invocation;
import com.marcusprado02.sharedkernel.crosscutting.aop.adapter.spring.SpringInvocation;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.time.Clock;
import java.util.concurrent.ThreadLocalRandom;

@Aspect
public class RetryAspect extends AspectBase {

    public RetryAspect(Tracer tracer, Meter meter, Clock clock, boolean failOpen) {
        super(tracer, meter, clock, failOpen);
    }

    @Around("@within(com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Retryable) || " +
            "@annotation(com.marcusprado02.sharedkernel.crosscutting.aop.annotations.Retryable)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        return super.invoke(new SpringInvocation(pjp));
    }

    @Override
    public Object invoke(Invocation inv) throws Throwable {
        var ann = inv.findAnnotation(Retryable.class).orElseThrow();
        int attempts = 0;
        long backoff = Math.max(0, ann.backoffMs());

        while (true) {
            attempts++;
            try {
                return inv.proceed();
            } catch (Throwable t) {
                if (!shouldRetry(t, ann.retryOn()) || attempts >= ann.maxAttempts()) throw t;
                long jitter = ThreadLocalRandom.current().nextLong(Math.max(1, backoff / 4));
                Thread.sleep(Math.min(5_000L, backoff + jitter)); // cap 5s
                backoff = Math.min(5_000L, backoff * 2);
            }
        }
    }

    private boolean shouldRetry(Throwable t, Class<? extends Throwable>[] types) {
        for (var c : types) if (c.isAssignableFrom(t.getClass())) return true;
        return false;
    }
}
