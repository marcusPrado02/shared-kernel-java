package com.marcusprado02.sharedkernel.domain.factory;

@FunctionalInterface
public interface DomainFactory<T> {
    T create();
}