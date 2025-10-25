package com.marcusprado02.sharedkernel.domain.snapshot.model;

public record SnapshotRecord(
        SnapshotId id,
        SnapshotMetadata metadata,
        SnapshotPayload payload
) {}
