package com.marcusprado02.sharedkernel.infrastructure.search;

@FunctionalInterface
public interface ProjectionMapper<S, T> {
    T map(S source, FieldMask mask);
}
