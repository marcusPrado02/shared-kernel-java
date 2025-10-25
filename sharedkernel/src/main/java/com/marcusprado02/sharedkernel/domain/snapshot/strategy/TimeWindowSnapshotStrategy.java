package com.marcusprado02.sharedkernel.domain.snapshot.strategy;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotMetadata;


public final class TimeWindowSnapshotStrategy<S,E> implements SnapshotStrategy<S,E> {
    private final Duration maxAge;

    public TimeWindowSnapshotStrategy(Duration maxAge) {
        if (maxAge.isNegative() || maxAge.isZero()) throw new IllegalArgumentException();
        this.maxAge = maxAge;
    }

    @Override
    public Decision shouldSnapshot(S state, E lastEvent, long since, long version,
                                   Optional<SnapshotMetadata> lastMeta, Instant now) {
        boolean take = lastMeta.map(m -> Duration.between(m.createdAt(), now).compareTo(maxAge) >= 0)
                               .orElse(true); // se nunca houve snapshot, tire
        return take
               ? Decision.yes("time_window_expired", Map.of("maxAge", maxAge.toString()))
               : Decision.no("time_window_active", Map.of("maxAge", maxAge.toString()));
    }
}
