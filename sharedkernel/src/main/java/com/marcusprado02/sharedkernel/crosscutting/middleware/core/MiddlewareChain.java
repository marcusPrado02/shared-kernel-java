package com.marcusprado02.sharedkernel.crosscutting.middleware.core;

@FunctionalInterface
public interface MiddlewareChain<T, R> {
    R next(T request) throws Exception;
}
