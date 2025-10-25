package com.marcusprado02.sharedkernel.domain.snapshot.strategy;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotMetadata;


public final class IntervalSnapshotStrategy<S, E> implements SnapshotStrategy<S, E> {
    private final long interval;

    public IntervalSnapshotStrategy(long interval) {
        if (interval <= 0) throw new IllegalArgumentException("interval must be > 0");
        this.interval = interval;
    }

    @Override
    public Decision shouldSnapshot(S state, E lastEvent, long since, long version,
                                   Optional<SnapshotMetadata> lastMeta, Instant now) {
        boolean take = since >= interval;
        return take
               ? Decision.yes("interval_reached", Map.of("since", since, "interval", interval))
               : Decision.no("interval_not_reached", Map.of("since", since, "interval", interval));
    }
}
