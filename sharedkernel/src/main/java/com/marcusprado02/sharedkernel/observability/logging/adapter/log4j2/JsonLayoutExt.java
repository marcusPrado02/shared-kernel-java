package com.marcusprado02.sharedkernel.observability.logging.adapter.log4j2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.observability.logging.LogContext;
import com.marcusprado02.sharedkernel.observability.logging.LogEnricher;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Plugin(name = "JsonLayoutExt", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class JsonLayoutExt extends AbstractStringLayout {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static volatile LogEnricher enricher; // opcional; pode ficar sem uso
    private static volatile LogContext context;   // opcional

    protected JsonLayoutExt(Charset charset) { super(charset); }

    @PluginFactory
    public static JsonLayoutExt createLayout() { return new JsonLayoutExt(StandardCharsets.UTF_8); }

    public static void setEnricher(LogEnricher e){ enricher = e; }
    public static void setContext(LogContext c){ context = c; }

    @Override
    public String toSerializable(LogEvent ev) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("timestamp", Instant.ofEpochMilli(ev.getTimeMillis()).toString());
            payload.put("severity", ev.getLevel() != null ? ev.getLevel().name() : null);
            payload.put("logger", ev.getLoggerName());
            payload.put("thread", ev.getThreadName());
            payload.put("message", ev.getMessage() != null ? ev.getMessage().getFormattedMessage() : null);
            payload.put("context", ev.getContextData() != null ? ev.getContextData().toMap() : Map.of());

            if (ev.getThrown() != null) {
                Throwable th = ev.getThrown();
                payload.put("exception.type", th.getClass().getName());
                payload.put("exception.message", th.getMessage());
                // stacktrace simples (evite objetos pesados)
                StringBuilder sb = new StringBuilder();
                for (StackTraceElement ste : th.getStackTrace()) sb.append(ste).append("\n");
                payload.put("exception.stacktrace", sb.toString());
            }

            // Se quiser enriquecer, deixe o enricher transformar o mapa (se sua interface permitir)
            // if (enricher != null) payload = enricher.enrich(payload, context);

            return MAPPER.writeValueAsString(payload) + "\n";
        } catch (Exception e) {
            return "{\"severity\":\"ERROR\",\"message\":\"[JsonLayoutExt error] " + e.getMessage() + "\"}\n";
        }
    }
}
