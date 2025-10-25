package com.marcusprado02.sharedkernel.infrastructure.search;

import java.util.Map;

public record AggregationResult(String name, Map<String, Object> values) {}

