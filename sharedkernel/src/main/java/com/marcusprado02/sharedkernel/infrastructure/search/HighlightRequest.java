package com.marcusprado02.sharedkernel.infrastructure.search;

import java.util.Set;

public record HighlightRequest(Set<String> fields, int fragmentSize, int numberOfFragments, String preTag, String postTag) {}

