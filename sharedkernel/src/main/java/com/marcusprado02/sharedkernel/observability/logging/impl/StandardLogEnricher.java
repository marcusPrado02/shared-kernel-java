package com.marcusprado02.sharedkernel.observability.logging.impl;


import java.time.Instant;
import java.util.*;

import com.marcusprado02.sharedkernel.observability.logging.*;

public final class StandardLogEnricher implements LogEnricher {
    private final FieldPolicy policy;
    private final Redactor redactor;
    private final CorrelationProvider correlation;
    private final Sampler sampler;

    public StandardLogEnricher(FieldPolicy policy, Redactor redactor, CorrelationProvider correlation, Sampler sampler) {
        this.policy = policy==null? FieldPolicy.permissive() : policy;
        this.redactor = redactor==null? new Redactor() : redactor;
        this.correlation = correlation==null? CorrelationProvider.mdcDefault() : correlation;
        this.sampler = sampler==null? Sampler.always() : sampler;
    }

    @Override
    public Map<String, Object> enrich(LogEvent e, LogContext ctx) {
        try {
            if (!sampler.accept(e.severity, e.loggerName, e.message)) return Map.of("dropped", true);

            Map<String,Object> base = new LinkedHashMap<>();
            // Campos “top-level” normalizados
            base.put("ts", e.timestamp==null? Instant.now().toString() : e.timestamp.toString());
            base.put("severity", e.severity.name());
            base.put("logger", e.loggerName);
            base.put("thread", e.threadName);

            // Contexto estável
            if (ctx != null) {
                base.put("service", ctx.service);
                base.put("environment", ctx.environment);
                base.put("region", ctx.region);
                base.put("version", ctx.version);
                if (ctx.staticTags != null && !ctx.staticTags.isEmpty()) base.put("tags", ctx.staticTags);
            }

            // Correlação
            var corr = correlation.correlation();
            if (!corr.isEmpty()) base.put("correlation", corr);

            // Mensagem
            if (e.message != null) base.put("message", redactor.scrub(e.message));

            // Extras
            Map<String,Object> extras = e.fields==null? Map.of() : e.fields;
            extras = redactor.scrubMap(extras);
            extras = policy.apply(extras);
            if (!extras.isEmpty()) base.putAll(extras);

            // Erro/stacktrace
            if (e.error != null) {
                base.put("error.type", e.error.getClass().getName());
                base.put("error.message", redactor.scrub(e.error.getMessage()));
                base.put("error.stacktrace", stacktrace(e.error, 4096)); // limita tamanho
            }
            return base;
        } catch (Throwable t) {
            // Fail-safe: enriquecer nunca deve quebrar a aplicação
            return Map.of("ts", Instant.now().toString(), "severity", e.severity.name(), "logger", e.loggerName,
                          "message", "[LogEnricherError] " + t.getMessage());
        }
    }

    private String stacktrace(Throwable t, int maxLen) {
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new java.io.PrintWriter(sw));
        String s = sw.toString();
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }
}
