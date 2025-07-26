package com.marcusprado02.sharedkernel.domain.specification;

import java.util.Objects;

public final class NamedSpecification<T> implements Specification<T> {

    private final String name;
    private final Specification<T> delegate;

    public NamedSpecification(String name, Specification<T> delegate) {
        this.name = name;
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return delegate.isSatisfiedBy(candidate);
    }

    public String getName() {
        return name;
    }

    public Specification<T> getInner() {
        return delegate;
    }

    @Override
    public String toString() {
        return "Specification[" + name + "]";
    }
}
