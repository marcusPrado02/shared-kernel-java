package com.marcusprado02.sharedkernel.infrastructure.payments.core;

import java.util.function.Supplier;

public interface CircuitBreaker {
    <T> T protect(Supplier<T> action);
    static CircuitBreaker noOp() {
        return new CircuitBreaker() {
            @Override
            public <T> T protect(Supplier<T> action) {
                return action.get();
            }
        };
    }
}
