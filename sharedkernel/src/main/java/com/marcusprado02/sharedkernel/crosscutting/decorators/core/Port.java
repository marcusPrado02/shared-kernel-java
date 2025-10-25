package com.marcusprado02.sharedkernel.crosscutting.decorators.core;

public interface Port<I, O> {
    O execute(I input) throws Exception;
}
