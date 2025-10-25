package com.marcusprado02.sharedkernel.infrastructure.email.core;


import java.time.Duration;
import java.util.Optional;

import com.marcusprado02.sharedkernel.infrastructure.email.model.EmailResponse;

public interface IdempotencyStore {
    Optional<EmailResponse> find(String idempotencyKey);
    void save(String idempotencyKey, EmailResponse response, Duration ttl);
}

