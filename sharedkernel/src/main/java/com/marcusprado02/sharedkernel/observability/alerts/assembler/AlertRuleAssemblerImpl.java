package com.marcusprado02.sharedkernel.observability.alerts.assembler;


import java.util.*;
import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.observability.alerts.core.AlertSeverity;
import com.marcusprado02.sharedkernel.observability.alerts.model.AlertCondition;
import com.marcusprado02.sharedkernel.observability.alerts.model.AlertRule;
import com.marcusprado02.sharedkernel.observability.alerts.model.SloSpec;
import com.marcusprado02.sharedkernel.observability.alerts.spi.RuleRenderer;

public final class AlertRuleAssemblerImpl implements AlertRuleAssembler {

    @Override
    public List<AlertRule> sloBurnRate(
            SloSpec slo,
            String errorRatioExpr,           // ex: sum(rate(http_requests_total{status=~"5.."}[W])) / sum(rate(http_requests_total[W]))
            List<String> windowsShort,       // ex: ["5m","30m"]
            List<String> windowsMid,         // ex: ["1h","6h"]
            double[] factors                 // ex: [14.4, 6] (crit/warn burn multipliers)
    ) {
        Objects.requireNonNull(slo); Objects.requireNonNull(errorRatioExpr);
        if (factors == null || factors.length < 2) factors = new double[]{14.4, 6.0}; // defaults Google SRE-like

        double errorBudget = 1.0 - slo.objective;

        String brShortCrit = burnConjunction(errorRatioExpr, windowsShort, errorBudget, factors[0]);
        String brMidCrit   = burnConjunction(errorRatioExpr, windowsMid,   errorBudget, factors[0]);
        String brShortWarn = burnConjunction(errorRatioExpr, windowsShort, errorBudget, factors[1]);
        String brMidWarn   = burnConjunction(errorRatioExpr, windowsMid,   errorBudget, factors[1]);

        List<AlertRule> out = new ArrayList<>();
        // CRITICAL: ambas short e mid
        out.add(rule(
                String.format("SLOBurnRateCritical{%s,%s}", slo.service, slo.sloName),
                String.format("(%s) and (%s)", brShortCrit, brMidCrit),
                "5m", labels(slo, AlertSeverity.CRITICAL), ann(slo, "SLO burn rate CRÍTICO", factors[0])
        ));
        // WARNING: ambas short e mid
        out.add(rule(
                String.format("SLOBurnRateWarning{%s,%s}", slo.service, slo.sloName),
                String.format("(%s) and (%s)", brShortWarn, brMidWarn),
                "15m", labels(slo, AlertSeverity.WARNING), ann(slo, "SLO burn rate AVISO", factors[1])
        ));
        return out;
    }

    private String burnConjunction(String ratioExprTemplate, List<String> windows, double errorBudget, double burnFactor) {
        if (windows == null || windows.isEmpty()) return "vector(0)";
        return windows.stream()
                .map(w -> ratioExprTemplate.replace("[W]", "["+w+"]") + String.format(" > %.10f", (burnFactor * errorBudget)))
                .collect(Collectors.joining(" and "));
    }

    @Override
    public AlertRule threshold(AlertCondition c, String summary, String desc, String runbook) {
        String expr = c.query.expr + " " + c.comparison + " " + c.threshold;
        return rule(
                c.name, expr, c.forDuration,
                Map.of("severity", c.severity.name().toLowerCase()),
                Map.of("summary", summary, "description", desc, "runbook", runbook==null? "" : runbook)
        );
    }

    private AlertRule rule(String name, String expr, String forD, Map<String,String> labels, Map<String,String> annotations) {
        return new AlertRule(name, expr, forD, labels, annotations);
    }

    private Map<String,String> labels(SloSpec slo, AlertSeverity sev){
        Map<String,String> m = new LinkedHashMap<>();
        m.put("severity", sev.name().toLowerCase());
        m.put("service", slo.service);
        m.put("slo", slo.sloName);
        m.putAll(slo.labels);
        return m;
    }

    private Map<String,String> ann(SloSpec slo, String title, double factor){
        return Map.of(
            "summary", title + " — " + slo.service + "/" + slo.sloName,
            "description", String.format("Consumo de orçamento de erro acima de %.2fx em múltiplas janelas.", factor),
            "runbook", "https://runbooks/"+slo.service+"/"+slo.sloName
        );
    }

    @Override
    public byte[] render(String group, List<AlertRule> rules, RuleRenderer renderer) throws Exception {
        return renderer.renderGroup(group, rules);
    }
}

