package com.marcusprado02.sharedkernel.adapters.in.rest.filters;

import com.marcusprado02.sharedkernel.contracts.api.ApiExchange;
import com.marcusprado02.sharedkernel.contracts.api.ApiFilter;
import com.marcusprado02.sharedkernel.contracts.api.ApiResponse;
import com.marcusprado02.sharedkernel.contracts.api.CtxKey;
import com.marcusprado02.sharedkernel.contracts.api.FilterDef;
import com.marcusprado02.sharedkernel.contracts.api.FilterResult;

public final class AuthzFilter implements ApiFilter {
    public static final CtxKey<String> PRINCIPAL = CtxKey.of("principal", String.class);
    private final Authorizer authz;

    public AuthzFilter(Authorizer authz){ this.authz = authz; }

    @Override public FilterResult apply(ApiExchange ex, Chain chain) throws Exception {
        if (!authz.isAuthorized(ex)) {
            var resp = ApiResponse.builder().status(401).header("www-authenticate","Bearer").finishedNow().build();
            ex.setResponse(resp);
            return new FilterResult.Halt(ex);
        }
        ex.ctx().put(PRINCIPAL, authz.principal(ex));
        return chain.proceed(ex);
    }

    public static FilterDef def(Authorizer authz, int order){
        return new AuthzFilter(authz).withNameOrderWhen("authz", order, _ex -> true);
    }
}
