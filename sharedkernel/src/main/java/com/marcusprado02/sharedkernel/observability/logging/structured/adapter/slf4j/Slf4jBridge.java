package com.marcusprado02.sharedkernel.observability.logging.structured.adapter.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marcusprado02.sharedkernel.observability.logging.Severity;
import com.marcusprado02.sharedkernel.observability.logging.structured.StructuredLogger;

import java.util.Map;

public final class Slf4jBridge {
    private final Logger slf4j;
    private final StructuredLogger structured;

    public Slf4jBridge(String name, StructuredLogger structured) {
        this.slf4j = LoggerFactory.getLogger(name);
        this.structured = structured;
    }

    public void info(String msg){ slf4j.info(msg); structured.info(msg); }
    public void info(String msg, Map<String,Object> fields){ slf4j.info(msg+" {}", fields); structured.log(Severity.INFO, msg, fields, null); }
    public void warn(String msg, Map<String,Object> fields){ slf4j.warn(msg+" {}", fields); structured.log(Severity.WARN, msg, fields, null); }
    public void error(String msg, Throwable t, Map<String,Object> fields){ slf4j.error(msg, t); structured.log(Severity.ERROR, msg, fields, t); }
}
