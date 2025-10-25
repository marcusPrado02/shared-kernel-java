package com.marcusprado02.sharedkernel.observability.alerts.assembler;

import java.util.List;

import com.marcusprado02.sharedkernel.observability.alerts.model.AlertCondition;
import com.marcusprado02.sharedkernel.observability.alerts.model.AlertRule;
import com.marcusprado02.sharedkernel.observability.alerts.model.SloSpec;
import com.marcusprado02.sharedkernel.observability.alerts.spi.RuleRenderer;

public interface AlertRuleAssembler {
    /** Gera alertas de SLO multi-janela/multi-burn-rate (MWMB). */
    List<AlertRule> sloBurnRate(SloSpec slo, String errorRatioExpr, List<String> windowsShort, List<String> windowsMid, double[] factors);

    /** Monta alertas unitários (threshold simples). */
    AlertRule threshold(AlertCondition condition, String summary, String description, String runbookUrl);

    /** Renderiza via renderer específico (Prometheus/Grafana). */
    byte[] render(String group, List<AlertRule> rules, RuleRenderer renderer) throws Exception;
}