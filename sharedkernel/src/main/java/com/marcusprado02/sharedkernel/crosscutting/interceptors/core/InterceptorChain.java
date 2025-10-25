package com.marcusprado02.sharedkernel.crosscutting.interceptors.core;

public interface InterceptorChain<TCtx extends InterceptionContext> {
    Object proceed(TCtx ctx) throws Exception;
    int index(); // útil p/ debug/ordem
}