package com.marcusprado02.sharedkernel.domain.model.base;


import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

/**
 * AggregateRoot “supremo”
 * - Mantém invariantes do agregado (validateAggregate())
 * - Orquestra mutações nas entidades-filhas com hooks (before/after)
 * - Publica DomainEvents com helpers semânticos
 * - Idempotência de comandos (opcional) via commandLog
 */
public abstract class AggregateRoot<ID extends Identifier> extends Entity<ID> {

    // --- Idempotência: rastreia comandos aplicados (por chave semântica) ---
    private final Set<String> commandLog = new HashSet<>();

    protected AggregateRoot(ID id) { super(id); }
    protected AggregateRoot(ID id, Version v, TenantId t, Instant c, Instant u, String cb, String ub, Instant d) {
        super(id, v, t, c, u, cb, ub, d);
    }

    /* ========== Ciclo de mutação seguro ========== */

    /**
     * Executa uma mutação do agregado garantindo:
     *  - validação antes/depois
     *  - atualização de auditoria
     *  - registro de eventos
     */
    protected final <T> T mutate(String actor, Supplier<T> action) {
        Objects.requireNonNull(actor, "actor must not be null");
        beforeMutation(actor);
        try {
            var result = action.get();
            validateAggregate();     // invariantes pós-mudança
            setAudit(actor);         // auditoria + updatedAt
            afterMutation(actor);
            return result;
        } catch (RuntimeException ex) {
            // opcional: rollback lógico em estruturas voláteis
            throw ex;
        }
    }

    protected final void mutate(String actor, Runnable runnable) {
        mutate(actor, () -> { runnable.run(); return null; });
    }

    /** Validação central do agregado (obrigatório). */
    protected abstract void validateAggregate();

    /** Hook para checagens prévias (e.g., estado do ciclo de vida). */
    protected void beforeMutation(String actor) { /* no-op por padrão */ }

    /** Hook para efeitos internos pós-mutação (recalcular totais, etc.). */
    protected void afterMutation(String actor) { /* no-op por padrão */ }

    /* ========== Gestão de eventos (helpers semânticos) ========== */

    protected void publishEvent(DomainEvent event) { recordEvent(event); }

    protected void publishEvent(Supplier<DomainEvent> eventFactory) { recordEvent(eventFactory.get()); }

    /* ========== Idempotência de comandos (opcional por agregado) ========== */

    /**
     * Garante que a operação só execute 1x para a chave informada.
     * Ex.: key = "PAY:paymentId" ou "RENEW:sagaId".
     */
    protected final <T> T once(String key, Supplier<T> action) {
        if (!commandLog.add(Objects.requireNonNull(key))) {
            // já executado → retorna null ou lance exceção específica
            return null;
        }
        return action.get();
    }

    protected final void once(String key, Runnable action) {
        once(key, () -> { action.run(); return null; });
    }

    public Set<String> appliedCommandKeys() { return Collections.unmodifiableSet(commandLog); }
}