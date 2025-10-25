package com.marcusprado02.sharedkernel.adapters.in.rest.filters;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

import com.marcusprado02.sharedkernel.contracts.api.ApiExchange;
import com.marcusprado02.sharedkernel.contracts.api.ApiFilter;
import com.marcusprado02.sharedkernel.contracts.api.ApiResponse;
import com.marcusprado02.sharedkernel.contracts.api.FilterDef;
import com.marcusprado02.sharedkernel.contracts.api.FilterResult;

public final class RateLimitFilter implements ApiFilter {
    private final RateLimiter limiter;
    private final Function<ApiExchange,String> keyFn;

    public RateLimitFilter(RateLimiter limiter, Function<ApiExchange,String> keyFn) {
        this.limiter = limiter; this.keyFn = keyFn;
    }

    @Override public FilterResult apply(ApiExchange ex, Chain chain) throws Exception {
        var key = keyFn.apply(ex);
        if (limiter.tryAcquire(key)) return chain.proceed(ex);

        var payload = ("{\"error\":\"rate_limited\",\"key\":\""+key+"\",\"ts\":\""+ Instant.now()+"\"}")
                .getBytes(StandardCharsets.UTF_8);
        var resp = ApiResponse.builder()
                .status(429).header("content-type","application/json")
                .header("retry-after","1").body(payload).finishedNow().build();
        ex.setResponse(resp);
        return new FilterResult.Halt(ex);
    }

    public static FilterDef def(RateLimiter limiter, Function<ApiExchange,String> keyFn, int order, java.util.function.Predicate<ApiExchange> when){
        return new RateLimitFilter(limiter, keyFn).withNameOrderWhen("rate-limit", order, when);
    }
}

