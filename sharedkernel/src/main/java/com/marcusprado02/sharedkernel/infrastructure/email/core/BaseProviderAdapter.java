package com.marcusprado02.sharedkernel.infrastructure.email.core;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.email.api.*;
import com.marcusprado02.sharedkernel.infrastructure.email.model.*;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.*;

public abstract class BaseProviderAdapter implements ProviderAdapter {

    protected EmailResponse ok(String providerId, String msgId, Map<String,Object> raw){
        return new EmailResponse(msgId, EmailStatus.QUEUED, EmailErrorCode.NONE, providerId, raw, Instant.now());
    }
    protected EmailResponse failed(String providerId, EmailErrorCode code, Map<String,Object> raw){
        return new EmailResponse(null, EmailStatus.FAILED, code, providerId, raw, Instant.now());
    }
    protected <T> T run(Policy p, java.util.concurrent.Callable<T> c){
        return p.limiter().acquire(() -> p.circuit().protect(c));
    }
    @Override public CompletableFuture<EmailResponse> sendAsync(EmailRequest req, Policy p){ 
        return CompletableFuture.supplyAsync(() -> send(req,p));
    }
    @Override public CompletableFuture<List<EmailResponse>> sendBulkAsync(BulkRequest req, Policy p){ 
        return CompletableFuture.supplyAsync(() -> sendBulk(req,p));
    }
}
