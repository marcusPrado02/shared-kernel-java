package com.marcusprado02.sharedkernel.infrastructure.search;

import java.util.Map;

public record FacetResult(String name, Map<String, Long> counts) {}

