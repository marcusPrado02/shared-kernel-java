package com.marcusprado02.sharedkernel.infrastructure.hateoas;

import java.net.URI;
import java.util.*;


/** Link HATEOAS canônico. */
public final class Link {
    private final Rel rel;
    private final URI href;                  // resolved; use TemplateLink para templated
    private final String type;               // content-type (ex.: application/json)
    private final String title;              // descrição
    private final boolean templated;
    private final Map<String, Object> meta;  // etag, deprecation, hreflang, name, profile, etc.
    private final List<Action> actions;      // affordances

    private Link(Rel rel, URI href, String type, String title, boolean templated,
                 Map<String,Object> meta, List<Action> actions) {
        this.rel = rel; this.href = href; this.type = type; this.title = title;
        this.templated = templated;
        this.meta = meta==null? Map.of() : Map.copyOf(meta);
        this.actions = actions==null? List.of() : List.copyOf(actions);
    }
    public static Builder builder(Rel rel){ return new Builder(rel); }

    public static final class Builder {
        private final Rel rel; private URI href; private String type; private String title;
        private boolean templated=false; private final Map<String,Object> meta = new LinkedHashMap<>();
        private final List<Action> actions = new ArrayList<>();
        public Builder(Rel rel){ this.rel = rel; }
        public Builder href(URI u){ this.href = u; return this; }
        public Builder type(String t){ this.type = t; return this; }
        public Builder title(String t){ this.title = t; return this; }
        public Builder templated(boolean v){ this.templated = v; return this; }
        public Builder meta(String k, Object v){ this.meta.put(k, v); return this; }
        public Builder action(Action a){ this.actions.add(a); return this; }
        public Link build(){ return new Link(rel, href, type, title, templated, meta, actions); }
    }

    // getters
    public Rel rel(){ return rel; } public URI href(){ return href; } public String type(){ return type; }
    public String title(){ return title; } public boolean templated(){ return templated; }
    public Map<String,Object> meta(){ return meta; } public List<Action> actions(){ return actions; }
}