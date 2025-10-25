package com.marcusprado02.sharedkernel.adapters.in.rest.filters;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CompletableFuture;
import  java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.marcusprado02.sharedkernel.contracts.api.ApiExchange;
import com.marcusprado02.sharedkernel.contracts.api.ApiFilter;
import com.marcusprado02.sharedkernel.contracts.api.ApiResponse;
import com.marcusprado02.sharedkernel.contracts.api.FilterDef;
import com.marcusprado02.sharedkernel.contracts.api.FilterResult;

public final class TimeoutFilter implements ApiFilter {
    private final long millis;
    public TimeoutFilter(long millis){ this.millis = millis; }

    @Override public FilterResult apply(ApiExchange ex, Chain chain) throws Exception {
        var t = new AtomicBoolean(false);
        var fut = CompletableFuture.supplyAsync(() -> {
            try { return chain.proceed(ex); }
            catch (Exception e){ throw new RuntimeException(e); }
        });
        try {
            var res = fut.get(millis, TimeUnit.MILLISECONDS);
            return res;
        } catch (TimeoutException te){
            t.set(true);
            var resp = ApiResponse.builder().status(504).finishedNow().build();
            ex.setResponse(resp);
            return new FilterResult.Halt(ex);
        }
    }

    public static FilterDef def(long ms, int order){ 
        return new TimeoutFilter(ms).withNameOrderWhen("timeout", order, _ex -> true); 
    }
}

