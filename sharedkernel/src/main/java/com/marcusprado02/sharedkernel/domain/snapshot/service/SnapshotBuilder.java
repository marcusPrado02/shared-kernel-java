package com.marcusprado02.sharedkernel.domain.snapshot.service;

public interface SnapshotBuilder<S> {
    byte[] serialize(S state);
}
