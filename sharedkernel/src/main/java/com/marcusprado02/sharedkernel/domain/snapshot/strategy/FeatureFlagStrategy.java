package com.marcusprado02.sharedkernel.domain.snapshot.strategy;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotMetadata;


public final class FeatureFlagStrategy<S,E> implements SnapshotStrategy<S,E> {
    private final Predicate<Context> flag;
    public record Context(String tenantId, String aggregateType, String aggregateId){}

    public FeatureFlagStrategy(Predicate<Context> flag) { this.flag = flag; }

    @Override
    public Decision shouldSnapshot(S state, E lastEvent, long since, long version,
                                   Optional<SnapshotMetadata> lastMeta, Instant now) {
        var ctx = lastMeta.map(m -> new Context(m.tenantId(), m.aggregateType(), m.aggregateId()))
                          .orElse(new Context("unknown","unknown","unknown"));
        boolean enabled = flag.test(ctx);
        return enabled ? Decision.yes("feature_flag_enabled", Map.of()) 
                       : Decision.no("feature_flag_disabled", Map.of());
    }
}