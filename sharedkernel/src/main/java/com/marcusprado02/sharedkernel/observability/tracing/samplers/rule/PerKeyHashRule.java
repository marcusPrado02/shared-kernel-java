package com.marcusprado02.sharedkernel.observability.tracing.samplers.rule;

import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventAttributes;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventSampler;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.SampleReason;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.util.DeterministicHash;

public final class PerKeyHashRule implements SamplerRule {
    private final double threshold; // 0..1 (ex.: 0.1 ⇒ 10% por chave)
    public PerKeyHashRule(double threshold){ this.threshold = Math.max(0, Math.min(1, threshold)); }

    @Override public EventSampler.Result match(EventAttributes e) {
        if (e.key == null) return null;
        double h = DeterministicHash.hashToUnit(e.key);
        return (h < threshold) ? EventSampler.Result.keep(SampleReason.KEY_HASH) : null;
    }
}
