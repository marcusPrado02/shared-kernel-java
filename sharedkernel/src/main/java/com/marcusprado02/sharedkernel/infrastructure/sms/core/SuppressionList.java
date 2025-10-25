package com.marcusprado02.sharedkernel.infrastructure.sms.core;

/** Lista de opt-out/suppression (STOP, blacklist, bloqueios legais). */
public interface SuppressionList {
    boolean isSuppressed(String tenantId, String e164);
    void addOptOut(String tenantId, String e164, String source); // via keyword STOP, painel etc.
}
