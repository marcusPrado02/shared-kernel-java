package com.marcusprado02.sharedkernel.crosscutting.interceptors.impl;

import java.time.Duration;

import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptionContext;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Interceptor;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptorChain;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

public class RateLimitInterceptor<TCtx extends InterceptionContext> implements Interceptor<TCtx> {
    private final RateLimiter rl;

    public RateLimitInterceptor(RateLimiter rl) { this.rl = rl; }

    @Override public Object around(TCtx ctx, InterceptorChain<TCtx> chain) throws Exception {
        var supplier = RateLimiter
            .decorateCheckedSupplier(rl, () -> chain.proceed(ctx));
        try {
            return supplier.get();
        } catch (Throwable e) {
            if (e instanceof Exception ex) throw ex;
            throw new RuntimeException(e);
        }
    }
    
    public static RateLimiter newLimiter(String name, int permitsPerSec, int timeoutMs) {
        var cfg = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(permitsPerSec)
                .timeoutDuration(Duration.ofMillis(timeoutMs))
                .build();
        return RateLimiter.of(name, cfg);
    }
}
