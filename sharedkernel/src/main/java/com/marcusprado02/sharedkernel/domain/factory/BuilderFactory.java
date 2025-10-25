package com.marcusprado02.sharedkernel.domain.factory;

public interface BuilderFactory<T> {
    T build(); // valida invariantes no build
}
