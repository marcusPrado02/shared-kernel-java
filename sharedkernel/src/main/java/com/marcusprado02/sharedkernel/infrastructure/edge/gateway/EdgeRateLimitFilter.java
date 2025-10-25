package com.marcusprado02.sharedkernel.infrastructure.edge.gateway;

import java.io.IOException;
import java.util.function.Function;

import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.LimitSpec;
import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.RateKey;
import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.RateLimiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public final class EdgeRateLimitFilter implements Filter {
    private final RateLimiter limiter;
    private final LimitSpec spec;
    private final Function<HttpServletRequest, RateKey> resolver;

    public EdgeRateLimitFilter(RateLimiter limiter, LimitSpec spec, Function<HttpServletRequest, RateKey> resolver) {
        this.limiter = limiter;
        this.spec = spec;
        this.resolver = resolver;
    }

    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
            throws IOException, ServletException {
        var req = (HttpServletRequest) sreq;
        var resp = (HttpServletResponse) sres;
        var key = resolver.apply(req);
        var d = limiter.evaluateAndConsume(key, spec);
        resp.setHeader("RateLimit-Limit", Integer.toString(spec.capacity()));
        resp.setHeader("RateLimit-Remaining", Long.toString(Math.max(0, d.remaining())));
        resp.setHeader("RateLimit-Reset", Long.toString(d.resetEpochSeconds()));
        if (!d.allowed()) { resp.setStatus(429); return; }
        chain.doFilter(req, resp);
    }
}

