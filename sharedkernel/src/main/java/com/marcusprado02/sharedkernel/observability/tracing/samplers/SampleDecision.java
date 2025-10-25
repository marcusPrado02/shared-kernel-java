package com.marcusprado02.sharedkernel.observability.tracing.samplers;

public enum SampleDecision { DROP, KEEP, DEFER } // DEFER: indique "avaliar no collector" (tail-based)

