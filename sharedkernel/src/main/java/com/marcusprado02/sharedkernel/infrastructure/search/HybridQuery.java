package com.marcusprado02.sharedkernel.infrastructure.search;

public record HybridQuery(String text, Float textWeight, VectorQuery vector, Float vectorWeight) {}
