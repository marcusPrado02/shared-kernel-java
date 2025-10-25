package com.marcusprado02.sharedkernel.application.port.example.impl;

import java.util.Optional;

public interface UserCommandPort {
    record CreateUser(String name, String email, java.util.Map<String, Object> metadata) {}
    record UpdateUser(String id, String name, String email, Optional<Boolean> active) {}

    void handle(CreateUser cmd, Context ctx);
    void handle(UpdateUser cmd, Context ctx);

    record Context(String requestId, String idempotencyKey, String tenantId, String subject, String version) {}
}
