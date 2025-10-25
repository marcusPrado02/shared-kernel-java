package com.marcusprado02.sharedkernel.adapters.in.rest.filters;

import com.marcusprado02.sharedkernel.contracts.api.ApiExchange;
import com.marcusprado02.sharedkernel.contracts.api.ApiFilter;
import com.marcusprado02.sharedkernel.contracts.api.FilterDef;
import com.marcusprado02.sharedkernel.contracts.api.FilterResult;

public final class ResponseCacheFilter implements ApiFilter {
    private final ResponseCache cache;
    private final long ttlMs;

    public ResponseCacheFilter(ResponseCache cache, long ttlMs){ this.cache = cache; this.ttlMs = ttlMs; }

    @Override public FilterResult apply(ApiExchange ex, Chain chain) throws Exception {
        if (!ex.request().method().equals("GET")) return chain.proceed(ex);
        var key = ex.request().path() + "?" + ex.request().query().toString();
        var hit = cache.get(key);
        if (hit.isPresent()){ 
            ex.setResponse(hit.get()); 
            return new FilterResult.Halt(ex); 
        }
        var res = chain.proceed(ex);
        ex.response().ifPresent(r -> { 
            if (r.status()==200) cache.put(key, r, ttlMs); 
        });
        return res;
    }

    public static FilterDef def(ResponseCache cache, long ttlMs, int order){
        return new ResponseCacheFilter(cache, ttlMs).withNameOrderWhen("resp-cache", order, _ex -> true);
    }
}
