package com.marcusprado02.sharedkernel.domain.snapshot.ports;

import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotRecord;

public interface SnapshotStore {
    void save(SnapshotRecord snapshot);
    Optional<SnapshotRecord> findLatest(String tenantId, String aggregateType, String aggregateId);
}