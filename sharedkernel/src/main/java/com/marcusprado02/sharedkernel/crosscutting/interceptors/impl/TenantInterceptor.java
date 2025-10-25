package com.marcusprado02.sharedkernel.crosscutting.interceptors.impl;

import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptionContext;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Interceptor;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptorChain;

public class TenantInterceptor<TCtx extends InterceptionContext> implements Interceptor<TCtx> {
    @Override public Object around(TCtx ctx, InterceptorChain<TCtx> chain) throws Exception {
        String tenant = (String) ctx.attributes().getOrDefault("tenantId", "default");
        ctx.carrier().set("X-Tenant-ID", tenant);
        return chain.proceed(ctx);
    }
}
