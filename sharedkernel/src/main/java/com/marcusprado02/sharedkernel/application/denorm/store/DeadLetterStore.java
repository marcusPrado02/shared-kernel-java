package com.marcusprado02.sharedkernel.application.denorm.store;

public interface DeadLetterStore {
    void put(String projectionName, String eventId, String type, String reason, String payloadJson);
}
