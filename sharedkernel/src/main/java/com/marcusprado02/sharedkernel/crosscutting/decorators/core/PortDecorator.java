package com.marcusprado02.sharedkernel.crosscutting.decorators.core;

import java.util.Objects;

public abstract class PortDecorator<I, O> implements Port<I, O> {
    protected final Port<I, O> delegate;

    protected PortDecorator(Port<I, O> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public O execute(I input) throws Exception {
        return delegate.execute(input);
    }
}
