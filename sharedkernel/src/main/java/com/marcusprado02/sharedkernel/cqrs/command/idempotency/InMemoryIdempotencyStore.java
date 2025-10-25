package com.marcusprado02.sharedkernel.cqrs.command.idempotency;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Store de idempotência em memória (para dev/testes).
 * Suporta:
 *  - visto/mark por (scope,key)
 *  - leasing (tryClaim) com TTL para evitar duplicidade concorrente
 *  - confirm para finalizar processamento bem-sucedido
 *
 * NOTA: Em produção use Redis/DB único com chaves únicas + TTL.
 */
public final class InMemoryIdempotencyStore implements IdempotencyStore {

    /** Marcações definitivas de processamento concluído. */
    private final ConcurrentMap<String, Long> completed = new ConcurrentHashMap<>();

    /** Leases em andamento por IdKey. */
    private final ConcurrentMap<IdKey, Lease> claims = new ConcurrentHashMap<>();

    private static final class Lease {
        final String owner;
        final long expiresAtMillis;
        Lease(String owner, long expiresAtMillis) { this.owner = owner; this.expiresAtMillis = expiresAtMillis; }
        boolean expired(long now) { return now >= expiresAtMillis; }
    }

    private static String ck(String scope, String key) {
        // chave composta estável
        return scope + "|" + key;
    }

    @Override
    public boolean seen(String scope, String key) {
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(key, "key");
        purgeExpired(); // limpeza oportunista
        return completed.containsKey(ck(scope, key));
    }

    @Override
    public void mark(String scope, String key) {
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(key, "key");
        completed.put(ck(scope, key), System.currentTimeMillis());
    }

    @Override
    public boolean tryClaim(IdKey id, Duration ttl, String owner) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ttl, "ttl");
        Objects.requireNonNull(owner, "owner");
        purgeExpired();

        final long now = System.currentTimeMillis();
        final long exp = now + Math.max(1, ttl.toMillis());

        // Regra:
        // - Se não há lease ou está expirado -> concede ao 'owner'
        // - Se já é do mesmo owner -> renova
        // - Se é de outro owner e não expirou -> nega
        return claims.compute(id, (k, existing) -> {
            if (existing == null || existing.expired(now)) {
                return new Lease(owner, exp);
            }
            if (existing.owner.equals(owner)) {
                return new Lease(owner, exp); // renova
            }
            return existing; // permanece com o outro owner
        }).owner.equals(owner);
    }

    @Override
    public void confirm(IdKey id) {
        Objects.requireNonNull(id, "id");
        // Ajuste os acessores abaixo se seu IdKey não tiver scope()/key()
        mark(id.scope(), id.naturalKey());
        claims.remove(id);
    }

    /** Limpeza básica de leases expirados para não acumular memória. */
    private void purgeExpired() {
        final long now = System.currentTimeMillis();
        claims.entrySet().removeIf(e -> e.getValue().expired(now));
        // Opcional: política de retenção para 'completed' (ex.: TTL) — aqui mantemos indefinidamente
    }

    @Override
    public boolean seen(String key) {
        // true se já vimos (não conseguiu adicionar => já existia)
        return completed.containsKey(key);
    }

    @Override
    public void record(String key) {
        completed.put(key, System.currentTimeMillis());
    }
}
