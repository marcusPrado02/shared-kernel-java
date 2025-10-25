package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query;

public record Agg(String field, AggFn fn, String as) {}
