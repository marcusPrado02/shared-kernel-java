package com.marcusprado02.sharedkernel.infrastructure.retry.http;

import org.springframework.web.reactive.function.client.*;

import com.marcusprado02.sharedkernel.infrastructure.retry.RetryContext;
import com.marcusprado02.sharedkernel.infrastructure.retry.ThirdPartyRetryPolicy;
import com.marcusprado02.sharedkernel.infrastructure.retry.RetryDecision;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

public class WebClientRetryFilter implements ExchangeFilterFunction {
    private final ThirdPartyRetryPolicy<ClientResponse, Throwable> policy;

    public WebClientRetryFilter(ThirdPartyRetryPolicy<ClientResponse, Throwable> policy) {
        this.policy = policy;
    }

    @Override public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        boolean isWrite = !request.method().matches("GET|HEAD|OPTIONS");
        boolean idempotent = request.headers().containsKey("Idempotency-Key");
        var ctx = new RetryContext("http-call", request.url().getHost()+request.url().getPath(), "default",
                                   UUID.randomUUID(), null);

        return attempt(1, request, next, isWrite, idempotent, ctx);
    }

    private Mono<ClientResponse> attempt(int attempt, ClientRequest req, ExchangeFunction next,
                                         boolean isWrite, boolean idempotent, RetryContext ctx) {
        ClientRequest enriched = isWrite && !req.headers().containsKey("Idempotency-Key")
            ? ClientRequest.from(req).header("Idempotency-Key", ctx.idempotencyKey().toString()).build()
            : req;

        return next.exchange(enriched).flatMap(resp -> {
            RetryDecision d = policy.decide(attempt, ctx, resp, null, isWrite, idempotent);
            if (!d.shouldRetry()) return Mono.just(resp);
            resp.releaseBody(); // libera buffer
            return Mono.delay(d.backoffDelay()).flatMap(__ -> attempt(d.nextAttempt(), enriched, next, isWrite, idempotent, ctx));
        }).onErrorResume(err -> {
            RetryDecision d = policy.decide(attempt, ctx, null, (Throwable) err, isWrite, idempotent);
            if (!d.shouldRetry()) return Mono.error(err);
            return Mono.delay(d.backoffDelay()).flatMap(__ -> attempt(d.nextAttempt(), enriched, next, isWrite, idempotent, ctx));
        });
    }
}