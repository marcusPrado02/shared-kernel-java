package com.marcusprado02.sharedkernel.observability.logging.adapter.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.observability.logging.*;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public class JsonLogbackEncoder extends EncoderBase<ILoggingEvent> {
    private final ObjectMapper mapper = new ObjectMapper();
    private LogEnricher enricher;
    private LogContext context;
    private OutputStream outputStream;

    public void setEnricher(LogEnricher e){ this.enricher = e; }
    public void setContextInfo(LogContext ctx){ this.context = ctx; }
    public void setOutputStream(OutputStream os){ this.outputStream = os; }

    @Override public byte[] headerBytes(){ return null; }
    @Override public byte[] footerBytes(){ return null; }

    public void doEncode(ILoggingEvent ev) throws java.io.IOException {
        if (enricher == null) return;
        var sev = switch (ev.getLevel().levelStr) {
            case "TRACE" -> Severity.TRACE;
            case "DEBUG" -> Severity.DEBUG;
            case "INFO"  -> Severity.INFO;
            case "WARN"  -> Severity.WARN;
            default      -> Severity.ERROR;
        };
        var event = new LogEvent(Instant.ofEpochMilli(ev.getTimeStamp()), sev, ev.getLoggerName(),
                ev.getThreadName(), ev.getFormattedMessage(), ev.getThrowableProxy() == null ? null : proxyToThrowable(ev.getThrowableProxy()),
                ev.getMDCPropertyMap() == null ? Map.of() : (Map) ev.getMDCPropertyMap());

        Map<String,Object> json = enricher.enrich(event, context);
        var out = mapper.writeValueAsString(json) + "\n";
        outputStream.write(out.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    private Throwable proxyToThrowable(ch.qos.logback.classic.spi.IThrowableProxy p){
        if (p == null) return null;
        // Best-effort: embrulha como RuntimeException com a mensagem
        return new RuntimeException(p.getClassName()+": "+p.getMessage());
    }
    @Override
    public byte[] encode(ILoggingEvent event) {
        try {
            if (outputStream == null) return null;
            doEncode(event);
            return null;
        } catch (Throwable t){
            // Não deve lançar
            System.err.println("JsonLogbackEncoder: erro ao codificar log: "+t);
            return null;
        }
    }
}

