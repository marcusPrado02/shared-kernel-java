package com.marcusprado02.sharedkernel.domain.events.serde;

public interface EventSerDe<T> {
    byte[] serialize(T event);
    T deserialize(byte[] bytes, Class<T> type);
    String contentType();
}