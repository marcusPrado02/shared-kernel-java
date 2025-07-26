package com.marcusprado02.sharedkernel.domain.snapshot;


import java.util.Optional;

public interface SnapshotStrategy<T> {
    /**
     * Carrega o último snapshot do agregado.
     *
     * @param aggregateId identificador do agregado
     * @return Snapshot contendo estado e versão, ou vazio se não existir
     */
    Optional<Snapshot<T>> loadSnapshot(String aggregateId);

    /**
     * Persiste um snapshot do estado atual do agregado.
     *
     * @param aggregateId identificador do agregado
     * @param aggregate instância do agregado
     */
    void saveSnapshot(String aggregateId, T aggregate);
}
