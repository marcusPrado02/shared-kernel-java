package com.marcusprado02.sharedkernel.infrastructure.payments.api;

import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.payments.model.*;

public interface RefundOperations {
    RefundResponse refund(RefundRequest req, Policy policy);
    CompletableFuture<RefundResponse> refundAsync(RefundRequest req, Policy policy);
}
