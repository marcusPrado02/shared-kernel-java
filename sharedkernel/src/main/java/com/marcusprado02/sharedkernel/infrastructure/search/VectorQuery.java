package com.marcusprado02.sharedkernel.infrastructure.search;

public record VectorQuery(String field, float[] vector, int k, boolean normalize) {}

