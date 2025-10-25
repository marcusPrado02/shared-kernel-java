package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query;

public record TagFilter(String key, Op op, Object value) {}

