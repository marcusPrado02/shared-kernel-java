package com.marcusprado02.sharedkernel.domain.id.api;

public interface IdGenerator<T> {
    T next();
}
