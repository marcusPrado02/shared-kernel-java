package com.marcusprado02.sharedkernel.adapters.in.rest.filters;

import org.slf4j.MDC;

import com.marcusprado02.sharedkernel.contracts.api.ApiExchange;
import com.marcusprado02.sharedkernel.contracts.api.ApiFilter;
import com.marcusprado02.sharedkernel.contracts.api.CtxKey;
import com.marcusprado02.sharedkernel.contracts.api.FilterDef;
import com.marcusprado02.sharedkernel.contracts.api.FilterResult;

import java.util.UUID;

public final class CorrelationFilter implements ApiFilter {
    public static final CtxKey<String> CORR_ID = CtxKey.of("correlationId", String.class);
    private static final String HDR = "x-correlation-id";

    @Override public FilterResult apply(ApiExchange ex, Chain chain) throws Exception {
        var incoming = ex.request().headers().get(HDR);
        var cid = incoming != null ? incoming : UUID.randomUUID().toString();
        ex.ctx().put(CORR_ID, cid);
        MDC.put("cid", cid);
        try {
            var res = chain.proceed(ex);
            ex.response().ifPresent(r -> r.headers().put(HDR, cid));
            return res;
        } finally {
            MDC.remove("cid");
        }
    }

    public static FilterDef def(int order) {
        return new CorrelationFilter().withNameOrderWhen("correlation", order, _ex -> true);
    }
}
