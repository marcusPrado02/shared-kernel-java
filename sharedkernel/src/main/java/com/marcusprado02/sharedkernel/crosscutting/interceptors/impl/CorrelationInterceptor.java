package com.marcusprado02.sharedkernel.crosscutting.interceptors.impl;

import java.util.UUID;

import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptionContext;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Interceptor;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptorChain;

public class CorrelationInterceptor<TCtx extends InterceptionContext> implements Interceptor<TCtx> {
    private final String headerName;
    public CorrelationInterceptor(String headerName) { this.headerName = headerName; }

    @Override public Object around(TCtx ctx, InterceptorChain<TCtx> chain) throws Exception {
        String cid = ctx.carrier().get(headerName).orElseGet(() -> UUID.randomUUID().toString());
        ctx.carrier().set(headerName, cid);
        ctx.attributes().putIfAbsent("correlationId", cid);
        return chain.proceed(ctx);
    }
}

