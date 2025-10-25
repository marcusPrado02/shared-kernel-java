package com.marcusprado02.sharedkernel.events.spi;


public interface MessageBroker {
    void publish(String eventType, String key, String payloadJson, String tenantId, String traceparent);
}
