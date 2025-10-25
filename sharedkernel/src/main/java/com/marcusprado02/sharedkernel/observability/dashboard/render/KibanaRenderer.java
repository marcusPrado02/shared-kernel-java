package com.marcusprado02.sharedkernel.observability.dashboard.render;


import java.nio.charset.StandardCharsets;

import com.marcusprado02.sharedkernel.observability.dashboard.model.Dashboard;
import com.marcusprado02.sharedkernel.observability.dashboard.spi.DashboardRenderer;

/** Placeholder simples: gera 1 saved object dashboard NDJSON com title/tags. */
public final class KibanaRenderer implements DashboardRenderer {
    @Override public byte[] render(Dashboard d) {
        String ndjson = """
        {"type":"dashboard","id":"%s","attributes":{"title":"%s","description":"","timeRestore":false,"kibanaSavedObjectMeta":{"searchSourceJSON":"{}"}}, "references":[]}
        """.formatted(d.uid, d.title).trim();
        return ndjson.getBytes(StandardCharsets.UTF_8);
    }
    @Override public String format(){ return "kibana-ndjson"; }
}
