package com.marcusprado02.sharedkernel.observability.tracing;


import java.util.*;

public final class SpanConfig {
    public final String name;
    public final SpanKind kind;
    public final Map<String, Object> attributes;
    public final String parentSpanId; // opcional (null = current context)

    private SpanConfig(Builder b){
        this.name = Objects.requireNonNull(b.name);
        this.kind = b.kind == null ? SpanKind.INTERNAL : b.kind;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(b.attributes));
        this.parentSpanId = b.parentSpanId;
    }
    public static Builder builder(String name){ return new Builder(name); }
    public static final class Builder {
        private final String name;
        private SpanKind kind;
        private final Map<String,Object> attributes = new LinkedHashMap<>();
        private String parentSpanId;
        private Builder(String name){ this.name=name; }
        public Builder kind(SpanKind k){ this.kind=k; return this; }
        public Builder attr(String k, Object v){ if(k!=null && v!=null) attributes.put(k,v); return this; }
        public Builder attrs(Map<String,Object> m){ if(m!=null) attributes.putAll(m); return this; }
        public Builder parent(String spanId){ this.parentSpanId=spanId; return this; }
        public SpanConfig build(){ return new SpanConfig(this); }
    }
}
