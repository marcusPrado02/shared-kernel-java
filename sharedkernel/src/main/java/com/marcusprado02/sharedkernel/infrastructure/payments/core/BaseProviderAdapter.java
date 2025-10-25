package com.marcusprado02.sharedkernel.infrastructure.payments.core;


import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.marcusprado02.sharedkernel.infrastructure.payments.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.payments.model.*;
import com.marcusprado02.sharedkernel.infrastructure.payments.spi.*;

public abstract class BaseProviderAdapter implements ProviderAdapter {

    protected PaymentResponse withPolicy(Policy p, Supplier<PaymentResponse> s) {
        return p.circuitBreaker().protect(() -> p.retryPolicy().executeWithRetry(s));
    }

    protected RefundResponse withPolicyRefund(Policy p, java.util.function.Supplier<RefundResponse> s) {
        return p.circuitBreaker().protect(() -> p.retryPolicy().executeWithRetry(s));
    }

    protected PaymentResponse buildResponseOk(String providerId, String chargeId, Map<String,Object> raw) {
        return new PaymentResponse(chargeId, PaymentStatus.AUTHORIZED, PaymentErrorCode.NONE,
                providerId, chargeId, null, null, raw, Instant.now(), Instant.now());
    }

    protected PaymentResponse buildDeclined(String providerId, PaymentErrorCode code, Map<String,Object> raw) {
        return new PaymentResponse(null, PaymentStatus.DECLINED, code,
                providerId, null, null, null, raw, Instant.now(), Instant.now());
    }

    @Override public CompletableFuture<PaymentResponse> authorizeAsync(PaymentRequest req, Policy policy) {
        return CompletableFuture.supplyAsync(() -> authorize(req, policy));
    }

    @Override public CompletableFuture<RefundResponse> refundAsync(RefundRequest req, Policy policy) {
        return CompletableFuture.supplyAsync(() -> refund(req, policy));
    }
}
