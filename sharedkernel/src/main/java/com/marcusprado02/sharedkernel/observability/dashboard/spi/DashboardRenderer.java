package com.marcusprado02.sharedkernel.observability.dashboard.spi;

import com.marcusprado02.sharedkernel.observability.dashboard.model.Dashboard;

/** Converte Dashboard -> payload serializado (JSON/YAML/etc.). */
public interface DashboardRenderer {
    byte[] render(Dashboard d) throws Exception;
    String format(); // "grafana-json", "kibana-ndjson"
}

