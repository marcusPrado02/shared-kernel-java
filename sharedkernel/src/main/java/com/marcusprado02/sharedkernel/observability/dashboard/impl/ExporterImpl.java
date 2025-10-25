package com.marcusprado02.sharedkernel.observability.dashboard.impl;


import java.nio.file.*;

import com.marcusprado02.sharedkernel.observability.dashboard.core.Exporter;
import com.marcusprado02.sharedkernel.observability.dashboard.model.Dashboard;
import com.marcusprado02.sharedkernel.observability.dashboard.spi.DashboardPublisher;
import com.marcusprado02.sharedkernel.observability.dashboard.spi.DashboardRenderer;

public final class ExporterImpl implements Exporter {

    @Override
    public Result export(Dashboard d, DashboardRenderer r, DashboardPublisher p, Path out) throws Exception {
        byte[] rendered = r.render(d);
        boolean changed = true;

        // Se publisher informado, detecta drift
        if (p != null) {
            byte[] current = p.fetchCurrent(d.uid);
            if (current != null) changed = !java.util.Arrays.equals(normalize(current), normalize(rendered));
            String uid = p.upsert(d, rendered, contentType(r));
            return new Result(uid, changed, null);
        }

        // Sem publisher → salva arquivo
        if (out == null) out = Path.of("dashboards", r.format(), d.uid + "." + ext(r));
        Files.createDirectories(out.getParent());
        byte[] existing = Files.exists(out) ? Files.readAllBytes(out) : null;
        if (existing != null) changed = !java.util.Arrays.equals(normalize(existing), normalize(rendered));
        Files.write(out, rendered, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return new Result(d.uid, changed, out);
    }

    private byte[] normalize(byte[] bytes){
        // best-effort: remove espaços em branco redundantes p/ comparar
        String s = new String(bytes, java.nio.charset.StandardCharsets.UTF_8)
                .replaceAll("\\s+"," ");
        return s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    private String contentType(DashboardRenderer r){
        return switch (r.format()) {
            case "grafana-json" -> "application/json";
            case "kibana-ndjson" -> "application/x-ndjson";
            default -> "application/octet-stream";
        };
    }
    private String ext(DashboardRenderer r){
        return switch (r.format()) {
            case "grafana-json" -> "json";
            case "kibana-ndjson" -> "ndjson";
            default -> "bin";
        };
    }
}

