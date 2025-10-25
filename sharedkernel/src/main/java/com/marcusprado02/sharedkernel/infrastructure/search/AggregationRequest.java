package com.marcusprado02.sharedkernel.infrastructure.search;

import java.util.Map;

public record AggregationRequest(String name, String type, String field, Map<String, Object> params) {}

