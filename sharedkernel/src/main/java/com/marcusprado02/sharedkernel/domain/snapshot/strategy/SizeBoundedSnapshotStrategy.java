package com.marcusprado02.sharedkernel.domain.snapshot.strategy;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotMetadata;
import com.marcusprado02.sharedkernel.domain.snapshot.ports.SizeEstimator;

public final class SizeBoundedSnapshotStrategy<S, E> implements SnapshotStrategy<S, E> {
    private final SizeEstimator<S> estimator;
    private final long maxBytes;

    public SizeBoundedSnapshotStrategy(SizeEstimator<S> estimator, long maxBytes) {
        if (maxBytes <= 0) throw new IllegalArgumentException("maxBytes must be > 0");
        this.estimator = estimator;
        this.maxBytes = maxBytes;
    }

    @Override
    public Decision shouldSnapshot(S state, E lastEvent, long since, long version,
                                   Optional<SnapshotMetadata> lastMeta, Instant now) {
        long estimated = estimator.estimateBytes(state);
        boolean take = estimated >= maxBytes;
        return take
               ? Decision.yes("size_threshold", Map.of("estimatedBytes", estimated, "maxBytes", maxBytes))
               : Decision.no("size_below_threshold", Map.of("estimatedBytes", estimated, "maxBytes", maxBytes));
    }
}
