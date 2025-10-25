package com.marcusprado02.sharedkernel.domain.snapshot.strategy;

import java.time.Instant;
import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotMetadata;


public interface SnapshotStrategy<S, E> {

    /**
     * Decide se deve tirar snapshot após aplicar o último evento.
     *
     * @param currentState estado atual do agregado (após aplicar E)
     * @param lastEvent evento recém-aplicado
     * @param eventsSinceLastSnapshot quantos eventos desde o último snapshot (0 se inexistente)
     * @param currentVersion versão atual do agregado
     * @param lastSnapshotMeta metadados do último snapshot, se houver
     * @param now timestamp atual (Clock injetável para teste)
     */
    Decision shouldSnapshot(
            S currentState,
            E lastEvent,
            long eventsSinceLastSnapshot,
            long currentVersion,
            Optional<SnapshotMetadata> lastSnapshotMeta,
            Instant now
    );
}
