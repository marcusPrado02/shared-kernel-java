package com.marcusprado02.sharedkernel.infrastructure.payments.api;

import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.payments.model.PayoutRequest;
import com.marcusprado02.sharedkernel.infrastructure.payments.model.PayoutResponse;

public interface PayoutOperations {
    PayoutResponse payout(PayoutRequest req, Policy policy);
    CompletableFuture<PayoutResponse> payoutAsync(PayoutRequest req, Policy policy);
}
