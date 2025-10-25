package com.marcusprado02.sharedkernel.application.denorm.store;

public interface OffsetStore {
    boolean wasProcessed(String projectionName, String eventId);
    void markProcessed(String projectionName, String eventId, long seq);
    long lastSequence(String projectionName);
}
