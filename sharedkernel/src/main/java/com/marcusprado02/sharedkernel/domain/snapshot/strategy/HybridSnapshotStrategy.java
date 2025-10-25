package com.marcusprado02.sharedkernel.domain.snapshot.strategy;

import java.util.List;

import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotMetadata;

import java.time.Instant;
import java.util.*;

public final class HybridSnapshotStrategy<S,E> implements SnapshotStrategy<S,E> {

    public enum Mode { ANY, ALL, MAJORITY }

    private final List<SnapshotStrategy<S,E>> delegates;
    private final Mode mode;
    private final Hysteresis hysteresis;

    public HybridSnapshotStrategy(List<SnapshotStrategy<S,E>> delegates, Mode mode, Hysteresis hysteresis) {
        if (delegates == null || delegates.isEmpty()) throw new IllegalArgumentException("delegates required");
        this.delegates = List.copyOf(delegates);
        this.mode = Objects.requireNonNull(mode);
        this.hysteresis = Objects.requireNonNullElse(hysteresis, Hysteresis.none());
    }

    @Override
    public Decision shouldSnapshot(S state, E lastEvent, long since, long version,
                                   Optional<SnapshotMetadata> lastMeta, Instant now) {

        // Histerese por eventos/tempo para evitar thrashing
        if (lastMeta.isPresent()) {
            var meta = lastMeta.get();
            long ageMillis = Math.max(0, now.toEpochMilli() - meta.createdAt().toEpochMilli());
            long eventGap = Math.max(0, version - meta.aggregateVersion());
            if (eventGap < hysteresis.minEventGap() || ageMillis < hysteresis.minMillisGap()) {
                return Decision.no("hysteresis_active", Map.of(
                        "eventGap", eventGap, "minGap", hysteresis.minEventGap(),
                        "ageMillis", ageMillis, "minMillis", hysteresis.minMillisGap()
                ));
            }
        }

        int yes = 0; Map<String,Object> attrs = new LinkedHashMap<>();
        for (SnapshotStrategy<S,E> d : delegates) {
            Decision dec = d.shouldSnapshot(state, lastEvent, since, version, lastMeta, now);
            attrs.put(d.getClass().getSimpleName(), dec.reason());
            if (dec.takeSnapshot()) yes++;
        }

        boolean take = switch (mode) {
            case ANY -> yes >= 1;
            case ALL -> yes == delegates.size();
            case MAJORITY -> yes > delegates.size() / 2;
        };

        return take ? Decision.yes("hybrid_yes_"+mode, attrs)
                    : Decision.no("hybrid_no_"+mode, attrs);
    }
}

