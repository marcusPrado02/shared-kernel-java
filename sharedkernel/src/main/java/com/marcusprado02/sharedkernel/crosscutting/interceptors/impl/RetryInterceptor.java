package com.marcusprado02.sharedkernel.crosscutting.interceptors.impl;

import java.util.function.Predicate;

import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptionContext;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Interceptor;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptorChain;

public class RetryInterceptor<TCtx extends InterceptionContext> implements Interceptor<TCtx> {
    private final int maxAttempts; private final long backoffMs;
    private final Predicate<Throwable> retryOn;

    public RetryInterceptor(int maxAttempts, long backoffMs, Predicate<Throwable> retryOn) {
        this.maxAttempts = maxAttempts; this.backoffMs = backoffMs; this.retryOn = retryOn;
    }

    @Override public Object around(TCtx ctx, InterceptorChain<TCtx> chain) throws Exception {
        int attempts = 0; long wait = backoffMs;
        while (true) {
            attempts++;
            try { return chain.proceed(ctx); }
            catch (Exception e) {
                if (!(e instanceof RuntimeException re) || !retryOn.test(re) || attempts >= maxAttempts) throw e;
                Thread.sleep(wait); wait = Math.min(5000, wait * 2);
            }
        }
    }
}

