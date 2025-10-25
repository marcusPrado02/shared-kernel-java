package com.marcusprado02.sharedkernel.crosscutting.exception.core;

public class RateLimitMapper<Ctx> implements ExceptionMapper<io.github.resilience4j.ratelimiter.RequestNotPermitted, Ctx> {
    @Override public MappedError map(io.github.resilience4j.ratelimiter.RequestNotPermitted ex, Ctx ctx) {
        return MappedError.builder()
            .status(ErrorCode.RATE_LIMITED.status).code(ErrorCode.RATE_LIMITED.code)
            .title(ErrorCode.RATE_LIMITED.title)
            .detail("Too many requests; please retry later.")
            .header("Retry-After", "1") // opcional
            .build();
    }
}

