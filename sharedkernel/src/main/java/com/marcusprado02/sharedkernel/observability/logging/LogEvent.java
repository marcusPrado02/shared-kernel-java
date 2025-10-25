package com.marcusprado02.sharedkernel.observability.logging;

import java.time.Instant;
import java.util.*;

public final class LogEvent {
    public final Instant timestamp;
    public final Severity severity;
    public final String loggerName;
    public final String threadName;
    public final String message;
    public final Throwable error; // opcional
    public final Map<String, Object> fields; // extras (chaves já normalizadas)

    public LogEvent(Instant timestamp, Severity severity, String loggerName, String threadName,
                    String message, Throwable error, Map<String,Object> fields) {
        this.timestamp = timestamp; this.severity = severity; this.loggerName = loggerName;
        this.threadName = threadName; this.message = message; this.error = error;
        this.fields = fields == null ? Map.of() : Map.copyOf(fields);
    }

    public LogEvent withFields(Map<String,Object> more) {
        Map<String,Object> out = new LinkedHashMap<>(this.fields);
        if (more != null) out.putAll(more);
        return new LogEvent(timestamp, severity, loggerName, threadName, message, error, out);
    }
}

