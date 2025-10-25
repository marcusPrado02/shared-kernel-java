package com.marcusprado02.sharedkernel.crosscutting.interceptors.core;

@FunctionalInterface
public interface Interceptor<TCtx extends InterceptionContext> {
    Object around(TCtx ctx, InterceptorChain<TCtx> chain) throws Exception;
}

