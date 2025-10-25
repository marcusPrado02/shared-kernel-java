package com.marcusprado02.sharedkernel.domain.snapshot.service;

public interface SnapshotUpcaster<S> {
    S deserializeAndUpcast(byte[] json, int fromSchemaVersion);
}
