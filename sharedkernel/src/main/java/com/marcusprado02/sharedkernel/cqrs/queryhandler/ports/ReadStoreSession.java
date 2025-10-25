package com.marcusprado02.sharedkernel.cqrs.queryhandler.ports;

import java.util.Optional;

import com.marcusprado02.sharedkernel.cqrs.query.ConsistencyHint;

/** Abstrai sessão de leitura: permite escolher réplica, aplicar hints, RLS, etc. */
public interface ReadStoreSession {
    void applyConsistency(ConsistencyHint hint);
    void setTenant(String tenantId);                // RLS/filtro implícito
    Optional<String> currentReplica();              // observabilidade
}
