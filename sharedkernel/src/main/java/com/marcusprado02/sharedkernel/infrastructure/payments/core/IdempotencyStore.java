package com.marcusprado02.sharedkernel.infrastructure.payments.core;

import java.util.Optional;

import com.marcusprado02.sharedkernel.infrastructure.payments.model.PaymentResponse;
import com.marcusprado02.sharedkernel.infrastructure.payments.model.RefundResponse;

public interface IdempotencyStore {
    Optional<PaymentResponse> findPayment(String idempotencyKey);
    void savePayment(String idempotencyKey, PaymentResponse response);

    Optional<RefundResponse> findRefund(String idempotencyKey);
    void saveRefund(String idempotencyKey, RefundResponse response);
}
