package com.marcusprado02.sharedkernel.domain.snapshot.service;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.service.time.ClockProvider;
import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotId;
import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotMetadata;
import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotPayload;
import com.marcusprado02.sharedkernel.domain.snapshot.model.SnapshotRecord;
import com.marcusprado02.sharedkernel.domain.snapshot.ports.SnapshotCompressor;
import com.marcusprado02.sharedkernel.domain.snapshot.ports.SnapshotStore;
import com.marcusprado02.sharedkernel.domain.snapshot.strategy.Decision;
import com.marcusprado02.sharedkernel.domain.snapshot.strategy.SnapshotStrategy;

public final class Snapshotter<S, E> {

    private final SnapshotStore store;
    private final SnapshotStrategy<S, E> strategy;
    private final SnapshotBuilder<S> builder;
    private final SnapshotUpcaster upcaster;
    private final SnapshotCompressor compressor;
    private final ClockProvider clockProvider;

    public Snapshotter(SnapshotStore store,
                       SnapshotStrategy<S, E> strategy,
                       SnapshotBuilder<S> builder,
                       SnapshotUpcaster upcaster,
                       SnapshotCompressor compressor,
                       ClockProvider clockProvider) {
        this.store = store;
        this.strategy = strategy;
        this.builder = builder;
        this.upcaster = upcaster;
        this.compressor = compressor;
        this.clockProvider = clockProvider;
    }

    public Optional<SnapshotRecord> maybeSnapshot(
            String tenantId, String aggregateType, String aggregateId,
            long version, long eventsSinceLastSnapshot,
            S state, E lastEvent, int schemaVersion, Map<String,String> baseTags) {

        Instant now = Instant.now(clockProvider.clock());
        Optional<SnapshotRecord> last = store.findLatest(tenantId, aggregateType, aggregateId);
        Optional<SnapshotMetadata> meta = last.map(SnapshotRecord::metadata);

        Decision decision = strategy.shouldSnapshot(
                state, lastEvent, eventsSinceLastSnapshot, version, meta, now);

        if (!decision.takeSnapshot()) return Optional.empty();

        byte[] serialized = builder.serialize(state);
        byte[] compressed = compressor.compress(serialized);

        Map<String,String> tags = new HashMap<>(baseTags == null ? Map.of() : baseTags);
        tags.put("compressor", compressor.name());
        tags.put("estimatedBytes", String.valueOf(serialized.length));
        decision.attributes().forEach((k,v) -> tags.put("dec."+k, String.valueOf(v)));

        SnapshotMetadata newMeta = new SnapshotMetadata(
                tenantId, aggregateType, aggregateId, version,
                meta.map(SnapshotMetadata::lastEventSequence).orElse(version),
                schemaVersion, now, tags);

        SnapshotRecord record = new SnapshotRecord(
                SnapshotId.random(),
                newMeta,
                new SnapshotPayload(compressed, "application/json", "compressed:"+compressor.name())
        );

        store.save(record);
        return Optional.of(record);
    }

    /** Carrega o último snapshot e upcast/deserialize para o estado atual. */
    public Optional<S> loadLatestState(String tenantId, String aggregateType, String aggregateId) {
        return (Optional<S>) store.findLatest(tenantId, aggregateType, aggregateId).map(rec -> {
            byte[] json = compressor.decompress(rec.payload().data());
            return upcaster.deserializeAndUpcast(json, rec.metadata().schemaVersion());
        });
    }
}

