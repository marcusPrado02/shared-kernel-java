package com.marcusprado02.sharedkernel.observability.alerts.render;


import java.nio.charset.StandardCharsets;
import java.util.*;

import com.marcusprado02.sharedkernel.observability.alerts.model.AlertRule;
import com.marcusprado02.sharedkernel.observability.alerts.spi.RuleRenderer;

public final class GrafanaAlertRenderer implements RuleRenderer {
    @Override public byte[] renderGroup(String group, List<AlertRule> rules) {
        // Simplificado: 1 regra -> 1 alerta Grafana (datasource Prometheus)
        StringBuilder sb = new StringBuilder();
        sb.append("{\"folderTitle\":\"").append(group).append("\",\"alerts\":[");
        for (int i=0;i<rules.size();i++) {
            var r = rules.get(i);
            if (i>0) sb.append(",");
            sb.append("{\"title\":\"").append(r.alert).append("\",")
              .append("\"condition\":\"A\",")
              .append("\"data\":[{\"refId\":\"A\",\"relativeTime\":\"").append(r.forDuration==null? "5m":r.forDuration)
              .append("\",\"datasourceUid\":\"prometheus\",\"model\":{\"expr\":\"").append(r.expr.replace("\"","\\\""))
              .append("\"}}],")
              .append("\"labels\":").append(toJson(r.labels)).append(",")
              .append("\"annotations\":").append(toJson(r.annotations))
              .append("}");
        }
        sb.append("]}");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
    @Override public String format(){ return "grafana"; }

    private static String toJson(Map<String,String> m){
        StringBuilder sb = new StringBuilder("{");
        int i=0; for (var e : m.entrySet()){
            if (i++>0) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":\"").append(e.getValue().replace("\"","\\\"")).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
}

