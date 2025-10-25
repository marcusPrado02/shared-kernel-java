package com.marcusprado02.sharedkernel.crosscutting.interceptors.impl;

import com.marcusprado02.sharedkernel.crosscutting.auth.AuthzService;
import com.marcusprado02.sharedkernel.crosscutting.context.AuthContext;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptionContext;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Interceptor;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptorChain;

public class AuthInterceptor<TCtx extends InterceptionContext> implements Interceptor<TCtx> {
    private final AuthzService authz;
    public AuthInterceptor(AuthzService authz) { this.authz = authz; }

    @Override public Object around(TCtx ctx, InterceptorChain<TCtx> chain) throws Exception {
        var token = ctx.carrier().get("Authorization").orElse(null);
        AuthContext ac = authz.authenticate(token);        // decodifica, valida
        authz.authorize(ac, ctx.operation());              // ABAC/OPA/policies
        ctx.attributes().put("auth", ac);
        return chain.proceed(ctx);
    }
}

