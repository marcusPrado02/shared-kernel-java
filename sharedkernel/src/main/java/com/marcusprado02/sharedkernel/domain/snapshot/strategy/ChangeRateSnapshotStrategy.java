package com.marcusprado02.sharedkernel.domain.snapshot.strategy;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotMetadata;
import com.marcusprado02.sharedkernel.domain.snapshot.ports.SizeEstimator;

public final class ChangeRateSnapshotStrategy<S,E> implements SnapshotStrategy<S,E> {
    private final SizeEstimator<S> estimator;
    private final double deltaRatio; // e.g., 0.25 => 25%

    public ChangeRateSnapshotStrategy(SizeEstimator<S> estimator, double deltaRatio) {
        if (deltaRatio <= 0) throw new IllegalArgumentException();
        this.estimator = estimator;
        this.deltaRatio = deltaRatio;
    }

    @Override
    public Decision shouldSnapshot(S state, E lastEvent, long since, long version,
                                   Optional<SnapshotMetadata> lastMeta, Instant now) {
        long nowSize = estimator.estimateBytes(state);
        long ref = lastMeta.map(m -> (Long) Long.parseLong(m.tags().getOrDefault("estimatedBytes", "0")))
                           .map(Long::valueOf).orElse(0L);
        boolean take = ref == 0 ? true : (nowSize >= ref * (1.0 + deltaRatio));
        return take
               ? Decision.yes("change_rate_exceeded", Map.of("prev", ref, "now", nowSize, "ratio", deltaRatio))
               : Decision.no("change_rate_below", Map.of("prev", ref, "now", nowSize, "ratio", deltaRatio));
    }
}
