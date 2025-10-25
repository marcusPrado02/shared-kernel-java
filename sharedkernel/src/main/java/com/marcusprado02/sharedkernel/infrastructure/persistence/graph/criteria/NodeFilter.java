package com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria;

// NodeFilter 
public record NodeFilter(String field, Op op, Object value){}
