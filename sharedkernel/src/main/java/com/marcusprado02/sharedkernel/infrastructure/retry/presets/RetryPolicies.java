package com.marcusprado02.sharedkernel.infrastructure.retry.presets;


import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;

import com.marcusprado02.sharedkernel.infrastructure.retry.RetryBudget;
import com.marcusprado02.sharedkernel.infrastructure.retry.ThirdPartyRetryPolicy;
import com.marcusprado02.sharedkernel.infrastructure.retry.http.OkHttpRetryClassifier;
import com.marcusprado02.sharedkernel.infrastructure.retry.http.WebClientRetryClassifier;

public final class RetryPolicies {

    public static ThirdPartyRetryPolicy<okhttp3.Response, java.io.IOException> okHttpDefault(MeterRegistry m) {
        var classifier = new OkHttpRetryClassifier();
        var budget = RetryBudget.tokenBucket(50, 25); // até 50 retries instantâneos, reabastece 25/s
        var policy = new ThirdPartyRetryPolicy.Builder<okhttp3.Response, java.io.IOException>()
            .classifier(classifier)
            .maxAttempts(5)
            .baseDelay(Duration.ofMillis(100))
            .maxDelay(Duration.ofSeconds(2))
            .backoff(ThirdPartyRetryPolicy.Backoff.DECORRELATED_JITTER)
            .honorRetryAfter(true)
            .requireIdempotencyForWrites(true)
            .budget(budget)
            .timeLimiter((attempt, delay) -> delay) // poderia cortar se extrapolar SLA
            .build();
        // opcional: registrar contador por categoria via decorator do Interceptor (não mostrado)
        return policy;
    }

    public static ThirdPartyRetryPolicy<org.springframework.web.reactive.function.client.ClientResponse, Throwable>
    webClientDefault(MeterRegistry m) {
        return new ThirdPartyRetryPolicy.Builder<org.springframework.web.reactive.function.client.ClientResponse, Throwable>()
            .classifier(new WebClientRetryClassifier())
            .maxAttempts(4)
            .baseDelay(Duration.ofMillis(200))
            .maxDelay(Duration.ofSeconds(3))
            .backoff(ThirdPartyRetryPolicy.Backoff.EQUAL_JITTER)
            .honorRetryAfter(true)
            .requireIdempotencyForWrites(true)
            .budget(RetryBudget.tokenBucket(30, 15))
            .build();
    }
}
