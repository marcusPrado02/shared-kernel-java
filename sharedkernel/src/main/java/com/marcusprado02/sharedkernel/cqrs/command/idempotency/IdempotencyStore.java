package com.marcusprado02.sharedkernel.cqrs.command.idempotency;

import java.time.Duration;

public interface IdempotencyStore {
    /** @return true se a chave já foi vista (ou seja, deve rejeitar duplicado) */
    boolean seen(String key);
    /** Grava/solidifica a chave após sucesso. */
    void record(String key);

    /**
     * Tenta registrar a intenção. Retorna true se a chamada atual é a "dona" do token.
     * Se já existir (e não expirado), retorna false (duplicado).
     */
    boolean tryClaim(IdKey key, Duration ttl, String owner);

    /** Marca conclusão bem-sucedida (opcional: para auditoria/telemetria). */
    void confirm(IdKey key);

    /** Dedupe estilo "inbox" (por consumidor). */
    boolean seen(String consumer, String messageId);
    void mark(String consumer, String messageId);
}
