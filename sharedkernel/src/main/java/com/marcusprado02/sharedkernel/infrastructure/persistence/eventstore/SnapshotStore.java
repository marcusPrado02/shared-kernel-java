package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import java.time.Instant;
import java.util.Optional;

/** Persistência opcional de snapshots. */
public interface SnapshotStore<ID> {
  Optional<Snapshot<ID>> load(String streamId);
  void save(Snapshot<ID> snapshot);
  record Snapshot<ID>(String streamId, long revision, Instant at, ID aggregateState, EventMetadata metadata) {}
}