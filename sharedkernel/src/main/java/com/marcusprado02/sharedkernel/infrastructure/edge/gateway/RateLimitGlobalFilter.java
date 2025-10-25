package com.marcusprado02.sharedkernel.infrastructure.edge.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.LimitSpec;
import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.RateKey;
import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.RateKeyResolver;
import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.RateLimiter;
import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.RatePolicyMatcher;

import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private final RateLimiter limiter;
    private final LimitSpec defaultSpec;                // injete via config
    private final RateKeyResolver<ServerWebExchange> resolver;
    private final RatePolicyMatcher policyMatcher;      // decide spec por rota/tenant

    @Override public int getOrder() { return -200; }    // executa cedo

    @Override
    public Mono<Void> filter(ServerWebExchange ex, GatewayFilterChain chain) {
        // match() deve devolver Optional<LimitSpec>; se não, ajuste a interface (item 2)
        LimitSpec spec = policyMatcher.match(ex).orElse(defaultSpec);

        RateKey key = resolver.apply(ex);
        RateLimiter.Decision decision = limiter.evaluateAndConsume(key, spec);

        decorateHeaders(ex, spec, decision);

        if (!decision.allowed()) {
            ex.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return ex.getResponse().setComplete();
        }
        return chain.filter(ex);
    }

    private void decorateHeaders(ServerWebExchange ex, LimitSpec spec, RateLimiter.Decision d) {
        var headers = ex.getResponse().getHeaders();
        headers.add("RateLimit-Limit", Integer.toString(spec.capacity()));
        headers.add("RateLimit-Remaining", Long.toString(Math.max(0, d.remaining())));
        headers.add("RateLimit-Reset", Long.toString(d.resetEpochSeconds()));
        if (!d.allowed()) {
            long nowSec = System.currentTimeMillis() / 1000;
            headers.add("Retry-After", Long.toString(Math.max(1, d.resetEpochSeconds() - nowSec)));
        }
    }
}
