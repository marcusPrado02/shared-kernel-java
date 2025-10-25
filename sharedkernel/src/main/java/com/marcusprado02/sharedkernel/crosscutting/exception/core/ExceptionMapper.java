package com.marcusprado02.sharedkernel.crosscutting.exception.core;

@FunctionalInterface
public interface ExceptionMapper<T extends Throwable, Ctx> {
    MappedError map(T ex, Ctx context);
}
