package com.marcusprado02.sharedkernel.observability.metrics.core;

import java.util.*;

public final class MetricId {
    private final String namespace;     // ex: "http", "db", "orders"
    private final String name;          // ex: "server.requests"
    private final Unit unit;            // opcional (pode ser null)
    private final String description;   // opcional
    private final Map<String, String> baseTags;

    private MetricId(Builder b) {
        this.namespace = b.namespace;
        this.name = b.name;
        this.unit = b.unit;
        this.description = b.description;
        this.baseTags = Collections.unmodifiableMap(new LinkedHashMap<>(b.baseTags));
    }

    public static Builder builder(String namespace, String name) {
        return new Builder(namespace, name);
    }

    public MetricId withTags(Map<String, String> more) {
        Builder b = new Builder(namespace, name).unit(unit).description(description).tags(baseTags);
        if (more != null) b.tags(more);
        return b.build();
    }

    public String fqName() {
        return (namespace == null || namespace.isBlank()) ? name : namespace + "." + name;
    }

    public String namespace() { return namespace; }
    public String name() { return name; }
    public Unit unit() { return unit; }
    public String description() { return description; }
    public Map<String, String> baseTags() { return baseTags; }

    public static final class Builder {
        private final String namespace;
        private final String name;
        private Unit unit;
        private String description;
        private final Map<String, String> baseTags = new LinkedHashMap<>();

        private Builder(String namespace, String name) {
            this.namespace = Objects.requireNonNull(namespace);
            this.name = Objects.requireNonNull(name);
        }
        public Builder unit(Unit unit) { this.unit = unit; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder tag(String k, String v) { if (k!=null && v!=null) this.baseTags.put(k, v); return this; }
        public Builder tags(Map<String,String> tags) { if (tags!=null) this.baseTags.putAll(tags); return this; }
        public MetricId build() { return new MetricId(this); }
    }
}

