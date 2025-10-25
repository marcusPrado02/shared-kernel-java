package com.marcusprado02.sharedkernel.observability.logging.structured;

import java.time.Instant;
import java.util.*;

import com.marcusprado02.sharedkernel.observability.logging.Severity;

public final class StructuredRecord {
    public final Instant ts;
    public final Severity level;
    public final String logger;
    public final String thread;
    public final String message;
    public final Throwable error;                 // opcional
    public final Map<String, Object> fields;      // SEMPRE normalizados / PII-safe

    public StructuredRecord(Instant ts, Severity level, String logger, String thread,
                            String message, Throwable error, Map<String,Object> fields) {
        this.ts = ts; this.level = level; this.logger = logger; this.thread = thread;
        this.message = message; this.error = error;
        this.fields = fields == null ? Map.of() : Map.copyOf(fields);
    }

    public StructuredRecord withFields(Map<String,Object> more){
        if (more == null || more.isEmpty()) return this;
        Map<String,Object> m = new LinkedHashMap<>(this.fields);
        m.putAll(more);
        return new StructuredRecord(ts, level, logger, thread, message, error, m);
    }
}