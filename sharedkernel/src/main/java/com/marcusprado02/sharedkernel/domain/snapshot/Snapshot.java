package com.marcusprado02.sharedkernel.domain.snapshot;

import java.io.Serializable;

import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotMetadata;

public record Snapshot<A>(
    SnapshotMetadata metadata,
    A state
) implements Serializable {}
