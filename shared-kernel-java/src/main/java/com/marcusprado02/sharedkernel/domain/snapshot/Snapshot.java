package com.marcusprado02.sharedkernel.domain.snapshot;



import java.time.Instant;

public class Snapshot<T> {
    private final String aggregateId;
    private final long version;
    private final T state;
    private final Instant createdAt;

    public Snapshot(String aggregateId, long version, T state, Instant createdAt) {
        this.aggregateId = aggregateId;
        this.version = version;
        this.state = state;
        this.createdAt = createdAt;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public long getVersion() {
        return version;
    }

    public T getState() {
        return state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
