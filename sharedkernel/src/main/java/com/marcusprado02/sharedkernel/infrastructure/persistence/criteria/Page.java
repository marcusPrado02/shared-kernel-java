package com.marcusprado02.sharedkernel.infrastructure.persistence.criteria;

import java.util.List;

public record Page<T>(List<T> content, long total, int page, int size) {}
