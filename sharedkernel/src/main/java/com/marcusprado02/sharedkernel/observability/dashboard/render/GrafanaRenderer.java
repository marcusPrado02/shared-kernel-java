package com.marcusprado02.sharedkernel.observability.dashboard.render;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.marcusprado02.sharedkernel.observability.dashboard.model.Dashboard;
import com.marcusprado02.sharedkernel.observability.dashboard.model.Panel;
import com.marcusprado02.sharedkernel.observability.dashboard.model.QuerySpec;
import com.marcusprado02.sharedkernel.observability.dashboard.model.TemplatingVar;
import com.marcusprado02.sharedkernel.observability.dashboard.spi.DashboardRenderer;

import java.nio.charset.StandardCharsets;

public final class GrafanaRenderer implements DashboardRenderer {
    private static final ObjectMapper M = new ObjectMapper();

    @Override public byte[] render(Dashboard d) throws Exception {
        ObjectNode root = M.createObjectNode();
        root.put("uid", d.uid);
        root.put("title", d.title);
        root.put("schemaVersion", 38);
        root.putArray("tags").addAll(d.tags.stream().map(tag -> M.convertValue(tag, TextNode.class)).toList());
        root.put("timezone", "browser");

        // templating variables
        ObjectNode templ = root.putObject("templating").putArray("list").addObject().removeAll();
        ArrayNode list = root.with("templating").withArray("list");
        for (var v : d.variables) list.add(varToNode(v));

        // panels
        ArrayNode panels = root.putArray("panels");
        for (var p : d.panels) panels.add(panelToNode(p));

        // annotations (links)
        if (!d.annotations.isEmpty()) {
            ObjectNode ann = root.putObject("annotations").putArray("list").addObject().removeAll();
            ArrayNode al = root.with("annotations").withArray("list");
            d.annotations.forEach((k,v)-> {
                ObjectNode a = M.createObjectNode();
                a.put("name", k); a.put("type","dashboard"); a.put("enable", true);
                a.put("builtIn", 1); a.put("iconColor", "rgba(255,96,96,1)");
                a.put("datasource", "-- Grafana --");
                a.putObject("expr").put("text", v);
                al.add(a);
            });
        }

        ObjectNode payload = M.createObjectNode();
        payload.set("dashboard", root);
        payload.put("folderUid", d.folder==null? "" : d.folder);
        payload.put("message", "Automated export");
        payload.put("overwrite", true);

        return M.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
    }

    @Override public String format(){ return "grafana-json"; }

    /* helpers */
    private ObjectNode varToNode(TemplatingVar v) {
        ObjectNode n = M.createObjectNode();
        n.put("name", v.name);
        n.put("type", switch (v.kind) {
            case QUERY -> "query";
            case TEXT  -> "textbox";
            case CUSTOM-> "custom";
        });
        n.put("multi", v.multi);
        n.put("query", v.query==null? "" : v.query);
        if (v.regex != null && !v.regex.isBlank()) n.put("regex", v.regex);
        return n;
    }

    private ObjectNode panelToNode(Panel p){
        ObjectNode n = M.createObjectNode();
        n.put("title", p.title);
        n.put("type", switch (p.type) {
            case TIMESERIES -> "timeseries";
            case STAT -> "stat";
            case GAUGE -> "gauge";
            case BAR -> "barchart";
            case TABLE -> "table";
            case HEATMAP -> "heatmap";
        });
        ObjectNode grid = n.putObject("gridPos");
        grid.put("x", p.gridX); grid.put("y", p.gridY); grid.put("w", p.gridW); grid.put("h", p.gridH);

        // datasource
        if (p.datasource != null) {
            ObjectNode ds = n.putObject("datasource");
            ds.put("type", p.datasource.type());
            ds.put("uid", p.datasource.uid());
        }

        // field config (unit/thresholds)
        ObjectNode fc = n.putObject("fieldConfig");
        fc.putObject("defaults");
        ArrayNode overrides = fc.putArray("overrides");
        if (!p.thresholds.isEmpty()) {
            ObjectNode defs = fc.with("defaults");
            ObjectNode th = defs.putObject("thresholds");
            th.put("mode","absolute");
            ArrayNode steps = th.putArray("steps");
            steps.addObject().put("color","green").putNull("value");
            for (var t : p.thresholds) steps.addObject().put("color", t.color()).put("value", t.value());
        }
        // options extras (unit, decimals...)
        if (!p.options.isEmpty()) {
            n.set("options", M.valueToTree(p.options));
        }

        // targets
        ArrayNode targets = n.putArray("targets");
        for (var q : p.queries) {
            ObjectNode t = targets.addObject();
            t.put("refId", refId(q));
            t.put("expr", q.expr);
            if (q.interval != null) t.put("interval", q.interval);
            if (q.legend != null) t.put("legendFormat", q.legend);
            // datasource por target opcional
        }

        return n;
    }
    private String refId(QuerySpec q){ // A,B,C...
        int h = Math.abs(q.expr.hashCode());
        return String.valueOf((char)('A' + (h % 26)));
    }
}

