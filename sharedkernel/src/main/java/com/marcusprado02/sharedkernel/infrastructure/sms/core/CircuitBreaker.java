package com.marcusprado02.sharedkernel.infrastructure.sms.core;


import java.util.concurrent.Callable;

public interface CircuitBreaker {
    <T> T protect(Callable<T> action);

    static CircuitBreaker noOp() {
        return new CircuitBreaker() {
            @Override
            public <T> T protect(Callable<T> action) {
                try {
                    return action.call();
                } catch (Exception e) {
                    throw (e instanceof RuntimeException re) ? re : new RuntimeException(e);
                }
            }
        };
    }
}
