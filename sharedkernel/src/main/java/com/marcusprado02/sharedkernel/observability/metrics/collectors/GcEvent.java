package com.marcusprado02.sharedkernel.observability.metrics.collectors;

import java.time.Instant;
import java.util.Map;

public record GcEvent(Instant when, String gcName, String action, String cause,
                      GcPhase phase, long pauseNanos,
                      Map<String, PoolDelta> pools) {}