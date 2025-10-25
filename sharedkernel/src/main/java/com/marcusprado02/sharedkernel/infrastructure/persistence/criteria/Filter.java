package com.marcusprado02.sharedkernel.infrastructure.persistence.criteria;

public record Filter(String field, Op op, Object value) {}
