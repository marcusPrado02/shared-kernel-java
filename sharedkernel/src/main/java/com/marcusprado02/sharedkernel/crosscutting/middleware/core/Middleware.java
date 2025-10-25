package com.marcusprado02.sharedkernel.crosscutting.middleware.core;

@FunctionalInterface
public interface Middleware<T, R> {
    R invoke(T request, MiddlewareChain<T, R> chain) throws Exception;
}

