package com.marcusprado02.sharedkernel.observability.logging.structured.sink;

import java.io.*;
import java.nio.file.*;

import com.marcusprado02.sharedkernel.observability.logging.structured.LogSink;

public final class FileSink implements LogSink {
    private final BufferedOutputStream out;

    public FileSink(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        this.out = new BufferedOutputStream(Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
    }
    @Override public synchronized void write(byte[] payload) throws IOException {
        out.write(payload); out.write('\n');
    }
    @Override public void flush() throws IOException { out.flush(); }
    @Override public void close() throws IOException { out.flush(); out.close(); }
}