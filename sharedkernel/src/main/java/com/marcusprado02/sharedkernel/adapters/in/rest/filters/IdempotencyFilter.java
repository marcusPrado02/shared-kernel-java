package com.marcusprado02.sharedkernel.adapters.in.rest.filters;

import com.marcusprado02.sharedkernel.contracts.api.ApiExchange;
import com.marcusprado02.sharedkernel.contracts.api.ApiFilter;
import com.marcusprado02.sharedkernel.contracts.api.FilterDef;
import com.marcusprado02.sharedkernel.contracts.api.FilterResult;

public final class IdempotencyFilter implements ApiFilter {
    private final IdempotencyStore store;
    private final String header;

    public IdempotencyFilter(IdempotencyStore store, String header){ this.store=store; this.header=header; }

    @Override public FilterResult apply(ApiExchange ex, Chain chain) throws Exception {
        var key = ex.request().headers().get(header);
        if (key == null || key.isBlank()) return chain.proceed(ex);

        var cached = store.find(key);
        if (cached.isPresent()) {
            ex.setResponse(cached.get());
            return new FilterResult.Halt(ex);
        }
        var out = chain.proceed(ex);
        ex.response().ifPresent(resp -> store.save(key, resp));
        return out;
    }

    public static FilterDef def(IdempotencyStore store, int order){
        return new IdempotencyFilter(store, "Idempotency-Key").withNameOrderWhen("idempotency", order, _ex -> true);
    }
}
