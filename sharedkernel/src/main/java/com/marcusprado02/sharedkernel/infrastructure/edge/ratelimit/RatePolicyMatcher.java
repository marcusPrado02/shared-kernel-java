package com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit;


import java.util.Optional;
import org.springframework.web.server.ServerWebExchange;

public interface RatePolicyMatcher {
    Optional<LimitSpec> match(ServerWebExchange exchange);
}