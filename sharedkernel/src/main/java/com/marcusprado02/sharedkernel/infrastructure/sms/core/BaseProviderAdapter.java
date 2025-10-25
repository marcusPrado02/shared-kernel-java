// src/main/java/com/marcusprado02/sharedkernel/infrastructure/sms/core/BaseProviderAdapter.java
package com.marcusprado02.sharedkernel.infrastructure.sms.core;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.sms.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.sms.model.*;
import com.marcusprado02.sharedkernel.infrastructure.sms.spi.*;

public abstract class BaseProviderAdapter implements ProviderAdapter {

    /** Constrói uma resposta de sucesso preenchendo todos os campos exigidos por SmsResponse. */
    protected SmsResponse ok(SmsRequest req, String providerId, String msgId, Map<String,Object> raw){
        String to = req.to() != null ? req.to().e164() : null;
        String body = req.body();
        return new SmsResponse(
                msgId,
                SmsStatus.QUEUED,
                SmsErrorCode.NONE,
                req.idempotencyKey(),
                to,
                body,
                providerId,
                raw,
                Instant.now()
        );
    }

    /** Constrói uma resposta de falha preenchendo todos os campos exigidos por SmsResponse. */
    protected SmsResponse failed(SmsRequest req, String providerId, SmsErrorCode code, Map<String,Object> raw){
        String to = req.to() != null ? req.to().e164() : null;
        String body = req.body();
        return new SmsResponse(
                null,
                SmsStatus.FAILED,
                code,
                req.idempotencyKey(),
                to,
                body,
                providerId,
                raw,
                Instant.now()
        );
    }

    /** Encapsula rate limit + circuit breaker + retry, todos em Callable<T>. */
    protected <T> T run(Policy p, Callable<T> action){
        return p.rateLimiter().acquire(() ->
            p.circuit().protect(() ->
                p.retry().executeWithRetry(action)
            )
        );
    }

    @Override public CompletableFuture<SmsResponse> sendAsync(SmsRequest req, Policy policy) {
        return CompletableFuture.supplyAsync(() -> send(req, policy));
    }
    @Override public CompletableFuture<List<SmsResponse>> sendBulkAsync(BulkRequest req, Policy policy) {
        return CompletableFuture.supplyAsync(() -> sendBulk(req, policy));
    }
}
