package com.marcusprado02.sharedkernel.observability.metrics.collectors;

public record PoolDelta(String pool, long beforeUsed, long afterUsed, long beforeCommitted, long afterCommitted){}

