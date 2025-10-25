package com.marcusprado02.sharedkernel.infrastructure.search;

import java.util.Set;

public record FieldMask(Set<String> include, Set<String> exclude) {}

