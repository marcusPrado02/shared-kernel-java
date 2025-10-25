package com.marcusprado02.sharedkernel.observability.logging.structured.sink;

import com.marcusprado02.sharedkernel.observability.logging.structured.LogSink;

public final class ConsoleSink implements LogSink {
    @Override public void write(byte[] payload) {
        System.out.write(payload, 0, payload.length);
        System.out.write('\n');
    }
}