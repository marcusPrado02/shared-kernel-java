package com.marcusprado02.sharedkernel.observability.tracing.samplers.rule;


import java.util.Set;

import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventAttributes;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventSampler;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.SampleDecision;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.SampleReason;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.util.Ewma;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.util.SlidingWindowCounter;

public final class ErrorAndSloRule implements SamplerRule {
    private final Set<String> errorSeverities;    // p.ex. WARN/ERROR/FATAL ⇒ KEEP
    private final Ewma latencyEwma;               // rastrear latência típica
    private final double outlierFactor;           // ex.: 2.5 ⇒ acima de 2.5× média ⇒ KEEP
    private final SlidingWindowCounter errorWin;  // erros recentes para overload

    public ErrorAndSloRule(Set<String> errorSeverities, double ewmaAlpha, double outlierFactor) {
        this.errorSeverities = errorSeverities==null? Set.of("ERROR","FATAL","WARN") : errorSeverities;
        this.latencyEwma = new Ewma(Math.max(0.05, Math.min(1.0, ewmaAlpha)));
        this.outlierFactor = Math.max(1.1, outlierFactor);
        this.errorWin = new SlidingWindowCounter(12, 5_000); // 60s
    }

    @Override public EventSampler.Result match(EventAttributes e) {
        // 1) Severidade
        if (e.severity != null && errorSeverities.contains(e.severity.toUpperCase())) {
            errorWin.inc();
            return EventSampler.Result.keep(SampleReason.ERROR);
        }
        // 2) Outlier de latência (se 'latency_ms' presente)
        Object lat = e.field("latency_ms");
        if (lat instanceof Number n) {
            double m = latencyEwma.add(n.doubleValue());
            if (n.doubleValue() > m * outlierFactor) {
                return EventSampler.Result.keep(SampleReason.OUTLIER);
            }
        }
        // 3) Overload: muitos erros recentes ⇒ aumente amostragem (mantém via DEFER para tail)
        long recentErrors = errorWin.sum();
        if (recentErrors > 50) {
            return new EventSampler.Result(SampleDecision.DEFER, SampleReason.SLO_BREACH, 0.8, 15); // 15s tail hint
        }
        return null;
    }
}
