package com.marcusprado02.sharedkernel.observability.dashboard.model;


import java.util.List;
import java.util.Map;

public final class Panel {
    public enum Type { TIMESERIES, STAT, GAUGE, BAR, TABLE, HEATMAP }
    public final String title;
    public final Type type;
    public final DatasourceRef datasource;
    public final List<QuerySpec> queries;
    public final List<Threshold> thresholds;
    public final int gridX, gridY, gridW, gridH;

    public final Map<String,Object> options; // renderer-specific extras (e.g., unit, decimals)

    public Panel(String title, Type type, DatasourceRef datasource, List<QuerySpec> queries,
                 List<Threshold> thresholds, int gridX, int gridY, int gridW, int gridH, Map<String,Object> options){
        this.title=title; this.type=type; this.datasource=datasource; this.queries=List.copyOf(queries);
        this.thresholds=thresholds==null? List.of(): List.copyOf(thresholds);
        this.gridX=gridX; this.gridY=gridY; this.gridW=gridW; this.gridH=gridH;
        this.options = options==null? Map.of(): Map.copyOf(options);
    }
}
