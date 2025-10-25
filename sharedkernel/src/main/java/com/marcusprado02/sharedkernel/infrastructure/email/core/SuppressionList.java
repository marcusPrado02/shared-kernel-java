package com.marcusprado02.sharedkernel.infrastructure.email.core;

public interface SuppressionList {
    /** Opt-out, bounces hard/complaints, blocklist regulatória. */
    boolean isSuppressed(String tenantId, String email);
    void add(String tenantId, String email, String reason);
}
