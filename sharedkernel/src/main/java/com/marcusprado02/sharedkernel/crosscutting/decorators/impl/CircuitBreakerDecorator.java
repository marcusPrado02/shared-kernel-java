package com.marcusprado02.sharedkernel.crosscutting.decorators.impl;

import com.marcusprado02.sharedkernel.crosscutting.decorators.core.Port;
import com.marcusprado02.sharedkernel.crosscutting.decorators.core.PortDecorator;

public class CircuitBreakerDecorator<I,O> extends PortDecorator<I,O> {
    private final io.github.resilience4j.circuitbreaker.CircuitBreaker cb;

    public CircuitBreakerDecorator(Port<I,O> delegate, io.github.resilience4j.circuitbreaker.CircuitBreaker cb) {
        super(delegate);
        this.cb = cb;
    }
    @Override
    public O execute(I input) throws Exception {
        try {
            return io.github.resilience4j.circuitbreaker.CircuitBreaker
                    .decorateCheckedSupplier(cb, () -> delegate.execute(input)).get();
        } catch (Throwable t) {
            if (t instanceof Exception e) throw e;
            throw new RuntimeException(t);
        }
    }
}

