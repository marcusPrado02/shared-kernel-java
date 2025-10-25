package com.marcusprado02.sharedkernel.observability.tracing.samplers.rule;

import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventAttributes;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventSampler;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.SampleReason;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.util.TokenBucket;

public final class RateLimitRule implements SamplerRule {
    private final TokenBucket global;
    private final TokenBucket.PerKey perKey;

    public RateLimitRule(double perSecGlobal, int burstGlobal, double perSecPerKey, int burstPerKey) {
        this.global = perSecGlobal > 0 ? new TokenBucket(perSecGlobal, burstGlobal) : null;
        this.perKey = perSecPerKey > 0 ? new TokenBucket.PerKey(perSecPerKey, burstPerKey) : null;
    }

    @Override public EventSampler.Result match(EventAttributes e) {
        if (perKey != null && e.key != null && perKey.allow(e.key)) {
            return EventSampler.Result.keep(SampleReason.RATE_LIMIT);
        }
        if (global != null && global.allow()) {
            return EventSampler.Result.keep(SampleReason.RATE_LIMIT);
        }
        return null;
    }
}
