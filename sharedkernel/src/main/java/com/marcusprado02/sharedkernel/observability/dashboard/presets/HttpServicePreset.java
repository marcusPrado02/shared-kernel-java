package com.marcusprado02.sharedkernel.observability.dashboard.presets;


import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.observability.dashboard.core.Backend;
import com.marcusprado02.sharedkernel.observability.dashboard.model.*;

public final class HttpServicePreset {

    public static Dashboard build(String service, String folderUid, DatasourceRef prom) {
        var vars = List.of(
            new TemplatingVar("namespace", TemplatingVar.Kind.QUERY, "label_values(kube_pod_info, namespace)", true, ""),
            new TemplatingVar("instance", TemplatingVar.Kind.QUERY, "label_values(up{job=\"kubernetes-pods\"}, instance)", true, "")
        );

        var p1 = new Panel(
            "HTTP Latency (P50/P90/P99)", Panel.Type.TIMESERIES, prom,
            List.of(
                new QuerySpec(Backend.GRAFANA, "histogram_quantile(0.5, sum by (le) (rate(http_server_requests_seconds_bucket{service=\""+service+"\"}[5m])))", "p50", "1m", Map.of()),
                new QuerySpec(Backend.GRAFANA, "histogram_quantile(0.9, sum by (le) (rate(http_server_requests_seconds_bucket{service=\""+service+"\"}[5m])))", "p90", "1m", Map.of()),
                new QuerySpec(Backend.GRAFANA, "histogram_quantile(0.99, sum by (le) (rate(http_server_requests_seconds_bucket{service=\""+service+"\"}[5m])))", "p99", "1m", Map.of())
            ),
            List.of(Threshold.warn(500), Threshold.crit(1000)), 0, 0, 24, 7,
            Map.of("legend", Map.of("displayMode","list"), "tooltip", Map.of("mode","single"), "unit","ms")
        );

        var p2 = new Panel(
            "HTTP Error Ratio", Panel.Type.STAT, prom,
            List.of(new QuerySpec(Backend.GRAFANA,
                "sum(rate(http_server_requests_seconds_count{service=\""+service+"\",status=~\"5..\"}[5m])) / sum(rate(http_server_requests_seconds_count{service=\""+service+"\"}[5m]))",
                "5xx ratio", "1m", Map.of())),
            List.of(new Threshold(0.01,"orange",">"), new Threshold(0.05,"red",">")), 0, 7, 8, 6,
            Map.of("unit","percentunit", "decimals",2)
        );

        var p3 = new Panel(
            "CPU / Memory", Panel.Type.TIMESERIES, prom,
            List.of(
                new QuerySpec(Backend.GRAFANA, "sum(rate(container_cpu_usage_seconds_total{container!=\"\",pod=~\""+service+".*\"}[5m]))", "cpu", "1m", Map.of()),
                new QuerySpec(Backend.GRAFANA, "sum(container_memory_working_set_bytes{container!=\"\",pod=~\""+service+".*\"})", "mem", "1m", Map.of())
            ),
            List.of(), 8, 7, 16, 6, Map.of("unit","bytes")
        );

        return new Dashboard(
            uid(service), service+" — SRE", folderUid,
            List.of("service:"+service,"sre"),
            vars,
            List.of(p1,p2,p3),
            Map.of("runbook","https://runbooks/"+service, "alerts","/d/alerts/"+service)
        );
    }

    private static String uid(String s){ return "svc-"+Integer.toHexString(Math.abs(s.hashCode())); }
}
