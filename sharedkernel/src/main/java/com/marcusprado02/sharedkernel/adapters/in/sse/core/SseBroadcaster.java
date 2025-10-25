package com.marcusprado02.sharedkernel.adapters.in.sse.core;

public interface SseBroadcaster {
    /** Fan-out para várias instâncias (Redis/Kafka/…); local pode só encadear para SseSink. */
    void broadcast(String topic, SseMessage msg);
}