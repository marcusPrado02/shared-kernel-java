package com.marcusprado02.sharedkernel.domain.snapshot;

import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotMetadata;

public interface SnapshotStrategy {
    boolean shouldSnapshot(SnapshotMetadata currentMeta, long eventsSinceLast, long bytesSinceLast);
}
