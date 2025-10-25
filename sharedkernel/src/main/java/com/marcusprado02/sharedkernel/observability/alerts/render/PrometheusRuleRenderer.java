package com.marcusprado02.sharedkernel.observability.alerts.render;


import java.nio.charset.StandardCharsets;
import java.util.*;

import com.marcusprado02.sharedkernel.observability.alerts.model.AlertRule;
import com.marcusprado02.sharedkernel.observability.alerts.spi.RuleRenderer;

public final class PrometheusRuleRenderer implements RuleRenderer {
    @Override public byte[] renderGroup(String group, List<AlertRule> rules) {
        StringBuilder sb = new StringBuilder();
        sb.append("groups:\n")
          .append("- name: ").append(escape(group)).append("\n")
          .append("  rules:\n");
        for (var r : rules) {
            sb.append("  - alert: ").append(escape(r.alert)).append("\n")
              .append("    expr: ").append(quote(r.expr)).append("\n");
            if (r.forDuration != null && !r.forDuration.isBlank())
                sb.append("    for: ").append(r.forDuration).append("\n");
            if (!r.labels.isEmpty()) {
                sb.append("    labels:\n");
                r.labels.forEach((k,v)-> sb.append("      ").append(k).append(": ").append(quote(v)).append("\n"));
            }
            if (!r.annotations.isEmpty()) {
                sb.append("    annotations:\n");
                r.annotations.forEach((k,v)-> sb.append("      ").append(k).append(": ").append(quote(v)).append("\n"));
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
    @Override public String format(){ return "prometheus"; }

    private static String quote(String s){ return "\"" + s.replace("\"","\\\"") + "\""; }
    private static String escape(String s){ return s.replace(" ", "_"); }
}