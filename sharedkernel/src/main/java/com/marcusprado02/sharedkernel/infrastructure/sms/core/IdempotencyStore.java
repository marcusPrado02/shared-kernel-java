package com.marcusprado02.sharedkernel.infrastructure.sms.core;

import java.time.Duration;
import java.util.Optional;

import com.marcusprado02.sharedkernel.infrastructure.sms.model.SmsResponse;

public interface IdempotencyStore {
    Optional<SmsResponse> find(String idempotencyKey);
    void save(String idempotencyKey, SmsResponse response, Duration ttl);
}
