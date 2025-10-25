package com.marcusprado02.sharedkernel.observability.dashboard.model;

import java.util.List;

public final class TemplatingVar {
    public enum Kind { QUERY, TEXT, CUSTOM }
    public final String name;
    public final Kind kind;
    public final String query;       // ex. label_values(job) / or comma list for CUSTOM
    public final boolean multi;      // permite selecionar múltiplos
    public final String regex;       // filtro extra

    public TemplatingVar(String name, Kind kind, String query, boolean multi, String regex){
        this.name=name; this.kind=kind; this.query=query; this.multi=multi; this.regex=regex;
    }
}
