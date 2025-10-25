package com.marcusprado02.sharedkernel.domain.snapshot;

import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.model.base.Identifier;

public interface SnapshotRepository<A,ID extends Identifier> {
    void save(Snapshot<A> snapshot);
    Optional<Snapshot<A>> findLatest(ID aggregateId, String aggregateType);
    void deleteOldSnapshots(ID aggregateId, String aggregateType, int keepLastN);
}
