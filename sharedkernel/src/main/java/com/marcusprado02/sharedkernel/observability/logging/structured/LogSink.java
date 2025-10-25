package com.marcusprado02.sharedkernel.observability.logging.structured;

public interface LogSink extends AutoCloseable {
    void write(byte[] payload) throws Exception;     // payload do Encoder (ex.: JSON bytes)
    default void flush() throws Exception {}
    @Override default void close() throws Exception {}
}
