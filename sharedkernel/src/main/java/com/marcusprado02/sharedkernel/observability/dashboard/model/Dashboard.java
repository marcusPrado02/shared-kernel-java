package com.marcusprado02.sharedkernel.observability.dashboard.model;

import java.util.List;
import java.util.Map;

public final class Dashboard {
    public final String uid;      // estável/determinístico (ex.: hash do nome + service)
    public final String title;
    public final String folder;   // destino no provedor
    public final List<String> tags;
    public final List<TemplatingVar> variables;
    public final List<Panel> panels;
    public final Map<String,String> annotations; // runbooks, links

    public Dashboard(String uid, String title, String folder, List<String> tags,
                     List<TemplatingVar> variables, List<Panel> panels, Map<String,String> annotations){
        this.uid=uid; this.title=title; this.folder=folder; this.tags=tags==null? List.of(): List.copyOf(tags);
        this.variables=variables==null? List.of(): List.copyOf(variables);
        this.panels=List.copyOf(panels);
        this.annotations = annotations==null? Map.of(): Map.copyOf(annotations);
    }
}