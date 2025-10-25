package com.marcusprado02.sharedkernel.observability.tracing.samplers;

public enum SampleReason {
    PERCENT, RATE_LIMIT, KEY_HASH, ERROR, SLO_BREACH, OUTLIER, OVERLOAD, ALWAYS, RULE, TIME_WINDOW, TAIL_HINT
}