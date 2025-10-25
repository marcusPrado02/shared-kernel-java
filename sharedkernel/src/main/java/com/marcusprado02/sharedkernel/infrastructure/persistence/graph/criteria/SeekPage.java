package com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria;

import java.util.List;
import java.util.Optional;

public record SeekPage<T>(List<T> content, Optional<SeekKey> next) {}
