package com.marcusprado02.sharedkernel.cqrs.handler.validation;

public interface BusinessValidator<C> {
    void validate(C command) throws IllegalArgumentException;
}