package com.marcusprado02.sharedkernel.observability.alerts.spi;

import java.util.List;

import com.marcusprado02.sharedkernel.observability.alerts.model.AlertRule;

/** Converte AlertRule -> artefatos (ex.: YAML PrometheusRule, Grafana JSON). */
public interface RuleRenderer {
    byte[] renderGroup(String groupName, List<AlertRule> rules) throws Exception;
    String format(); // "prometheus", "grafana", ...
}
