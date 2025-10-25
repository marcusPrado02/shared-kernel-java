package com.marcusprado02.sharedkernel.cqrs.command.spi;


import java.util.HashMap;
import java.util.Map;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

public interface TracerFacade {
    interface Span {
        void end();
    }
    final class Tags {
        private final Map<String,String> m = new HashMap<>();
        public static Tags of(){ return new Tags(); }
        public static Tags of(String k, Object v){ return new Tags().put(k,v); }
        public static Tags of(String k1, Object v1, String k2, Object v2){
            return new Tags().put(k1,v1).put(k2,v2);
        }
        public Tags put(String k, Object v){ if (v!=null) m.put(k, String.valueOf(v)); return this; }
        public Map<String,String> asMap(){ return Map.copyOf(m); }
    }

    Span startSpan(String name, Tags tags);
    void tag(Span span, String key, String value);
    void endSpan(Span span);
    String currentTraceparent();

    /** Implementação OpenTelemetry */
    static TracerFacade openTelemetry(Tracer tracer) {
        return new TracerFacade() {
            @Override public Span startSpan(String name, Tags tags) {
                var b = tracer.spanBuilder(name);
                tags.asMap().forEach((k,v) -> b.setAttribute(k, v));
                var s = b.startSpan();
                s.makeCurrent();
                return (Span) s;
            }
            @Override public void tag(Span span, String key, String value) {
                ((io.opentelemetry.api.trace.Span) span).setAttribute(key, value);
            }
            @Override public void endSpan(Span span) {
                ((io.opentelemetry.api.trace.Span) span).end();
                Context.current().makeCurrent();
            }
            @Override public String currentTraceparent() {
                // Retorne “traceparent” W3C se disponível; aqui deixamos vazio para simplificação
                return "";
            }
        };
    }
}
