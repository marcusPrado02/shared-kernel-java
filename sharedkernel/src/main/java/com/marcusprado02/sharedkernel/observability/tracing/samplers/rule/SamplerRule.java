package com.marcusprado02.sharedkernel.observability.tracing.samplers.rule;

import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventAttributes;
import com.marcusprado02.sharedkernel.observability.tracing.samplers.EventSampler;

public interface SamplerRule {
    /** Retorna null se a regra não se aplica; caso contrário uma decisão. */
    EventSampler.Result match(EventAttributes e);
}
