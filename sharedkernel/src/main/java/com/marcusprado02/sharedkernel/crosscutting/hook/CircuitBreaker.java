package com.marcusprado02.sharedkernel.crosscutting.hook;

import java.util.concurrent.*;

public interface CircuitBreaker {
    <T> T call(Callable<T> c) throws Exception;
    CircuitBreaker NOOP = new CircuitBreaker() {
        @Override
        public <T> T call(Callable<T> c) throws Exception {
            return c.call();
        }
    };
}
