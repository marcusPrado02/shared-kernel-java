package com.marcusprado02.sharedkernel.infrastructure.edge.gateway;

import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.LimitSpec;
import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.RateKey;
import com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit.RateLimiter;


import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class NettyRateLimitHandler implements Consumer<HttpServerRoutes> {

    private final RateLimiter limiter;
    private final LimitSpec spec;
    private final Function<HttpServerRequest, RateKey> resolver;
    /** Handler “próximo” na cadeia – injete quem realmente atende a rota. */
    private final BiFunction<HttpServerRequest, HttpServerResponse, Mono<Void>> next;

    public NettyRateLimitHandler(RateLimiter limiter,
                                 LimitSpec spec,
                                 Function<HttpServerRequest, RateKey> resolver,
                                 BiFunction<HttpServerRequest, HttpServerResponse, Mono<Void>> next) {
        this.limiter = limiter;
        this.spec = spec;
        this.resolver = resolver;
        this.next = next;
    }

    @Override
    public void accept(HttpServerRoutes routes) {
        routes.route(req -> true, (req, res) -> {
            var key = resolver.apply(req);
            var d = limiter.evaluateAndConsume(key, spec);

            res.addHeader("RateLimit-Limit", Integer.toString(spec.capacity()));
            res.addHeader("RateLimit-Remaining", Long.toString(Math.max(0, d.remaining())));
            res.addHeader("RateLimit-Reset", Long.toString(d.resetEpochSeconds()));

            if (!d.allowed()) {
                res.status(429);
                return res.send(); // corpo vazio no throttle
            }
            // Encaminha para o próximo handler real da aplicação
            return next.apply(req, res);
        });
    }
}

