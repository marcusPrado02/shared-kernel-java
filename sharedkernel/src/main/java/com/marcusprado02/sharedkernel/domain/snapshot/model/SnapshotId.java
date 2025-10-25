package com.marcusprado02.sharedkernel.domain.snapshot.model;

import java.util.Objects;
import java.util.UUID;

public final class SnapshotId {
    private final UUID value;
    private SnapshotId(UUID value) { this.value = Objects.requireNonNull(value); }
    public static SnapshotId random() { return new SnapshotId(UUID.randomUUID()); }
    public UUID value() { return value; }
    @Override public String toString() { return value.toString(); }
    @Override public boolean equals(Object o){ return (o instanceof SnapshotId s) && value.equals(s.value);}
    @Override public int hashCode(){ return value.hashCode(); }
}
