package com.marcusprado02.sharedkernel.observability.tracing.samplers;


import java.util.List;

import com.marcusprado02.sharedkernel.observability.tracing.samplers.rule.SamplerRule;

public final class CompositeEventSampler implements EventSampler {
    private final List<SamplerRule> rules;
    private final boolean defaultDrop;

    public CompositeEventSampler(List<SamplerRule> rules, boolean defaultDrop) {
        this.rules = List.copyOf(rules);
        this.defaultDrop = defaultDrop;
    }

    @Override public Result shouldSample(EventAttributes e) {
        for (var r : rules) {
            var res = r.match(e);
            if (res != null) return res;
        }
        return defaultDrop ? Result.drop() : Result.keep(SampleReason.ALWAYS);
    }
}
