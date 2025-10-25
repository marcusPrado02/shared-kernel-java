package com.marcusprado02.sharedkernel.observability.logging.structured;

import java.util.Map;

import com.marcusprado02.sharedkernel.observability.logging.Severity;

public interface StructuredLogger {
    void log(Severity level, String message, Map<String,Object> fields, Throwable error);

    default void trace(String msg){ log(Severity.TRACE, msg, Map.of(), null); }
    default void debug(String msg){ log(Severity.DEBUG, msg, Map.of(), null); }
    default void info (String msg){ log(Severity.INFO , msg, Map.of(), null); }
    default void warn (String msg){ log(Severity.WARN , msg, Map.of(), null); }
    default void error(String msg){ log(Severity.ERROR, msg, Map.of(), null); }
    default void error(String msg, Throwable t){ log(Severity.ERROR, msg, Map.of(), t); }

    /** Cria um logger "filho" com campos fixos (escopo). */
    StructuredLogger withFields(Map<String,Object> fixedFields);

    /** Nome do logger (para dashboards/filters). */
    String name();
}
