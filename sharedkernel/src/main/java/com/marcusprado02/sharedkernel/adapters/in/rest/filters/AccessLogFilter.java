package com.marcusprado02.sharedkernel.adapters.in.rest.filters;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import com.marcusprado02.sharedkernel.contracts.api.ApiExchange;
import com.marcusprado02.sharedkernel.contracts.api.ApiFilter;
import com.marcusprado02.sharedkernel.contracts.api.ApiResponse;
import com.marcusprado02.sharedkernel.contracts.api.FilterDef;
import com.marcusprado02.sharedkernel.contracts.api.FilterResult;

import java.util.Set;
import java.util.stream.Collectors;

public final class AccessLogFilter implements ApiFilter {
    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);
    private final Set<String> secretHeaders = Set.of("authorization","x-api-key","cookie");

    @Override public FilterResult apply(ApiExchange ex, Chain chain) throws Exception {
        var req = ex.request();
        var redactedHeaders = req.headers().entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(),
                e -> secretHeaders.contains(e.getKey().toLowerCase()) ? "***" : e.getValue()
        ));
        long t0 = System.nanoTime();
        try {
            var res = chain.proceed(ex);
            var status = ex.response().map(ApiResponse::status).orElse(-1);
            long dt = (System.nanoTime()-t0)/1_000_000;
            log.info("REQ {} {} hdr={} -> status={} dtMs={}", req.method(), req.path(), redactedHeaders, status, dt);
            return res;
        } catch (Exception e){
            long dt = (System.nanoTime()-t0)/1_000_000;
            log.error("REQ {} {} failed after {}ms: {}", req.method(), req.path(), dt, e.toString());
            throw e;
        }
    }

    public static FilterDef def(int order){ 
        return new AccessLogFilter().withNameOrderWhen("access-log", order, _ex -> true); 
    }
}
