package com.marcusprado02.sharedkernel.domain.snapshot.service;


import com.fasterxml.jackson.databind.ObjectMapper;

public final class JacksonSnapshotSerDe<S> implements SnapshotBuilder<S>, SnapshotUpcaster<S> {
    private final ObjectMapper mapper;
    private final Class<S> stateType;
    private final SnapshotUpgradePath<S> upgrades;
    public interface SnapshotUpgradePath<T> { T upgrade(T value, int fromSchemaVersion); }

    public JacksonSnapshotSerDe(ObjectMapper mapper, Class<S> stateType, SnapshotUpgradePath<S> upgrades){
        this.mapper = mapper; this.stateType = stateType; this.upgrades = upgrades;
    }

    @Override public byte[] serialize(S state){
        try { return mapper.writeValueAsBytes(state); }
        catch (Exception e){ throw new RuntimeException(e); }
    }

    @Override public S deserializeAndUpcast(byte[] json, int fromSchemaVersion){
        try {
            S raw = mapper.readValue(json, stateType);
            return upgrades.upgrade(raw, fromSchemaVersion);
        } catch (Exception e){ throw new RuntimeException(e); }
    }
}

