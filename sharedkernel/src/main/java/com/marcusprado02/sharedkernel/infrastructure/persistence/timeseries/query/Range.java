package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query;

import java.time.Instant;

public record Range(Instant from, Instant to) {}
