package com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit;


import java.util.function.Function;

/** Resolve chaves a partir da requisição edge. */
public interface RateKeyResolver<C> extends Function<C, RateKey> {}
