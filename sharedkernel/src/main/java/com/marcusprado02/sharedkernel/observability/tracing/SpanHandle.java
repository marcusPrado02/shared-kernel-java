package com.marcusprado02.sharedkernel.observability.tracing;


import java.util.Map;

public interface SpanHandle extends AutoCloseable {
    void setAttribute(String key, String value);
    void setAttribute(String key, long value);
    void setAttribute(String key, double value);
    void setAttribute(String key, boolean value);
    void addEvent(String name, Map<String, Object> attributes);
    void recordException(Throwable error, Map<String, Object> attributes);
    void setStatus(StatusCode code, String description);
    String spanId();
    String traceId();
    @Override void close(); // encerra o span
}
