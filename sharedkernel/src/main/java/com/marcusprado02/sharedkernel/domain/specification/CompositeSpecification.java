package com.marcusprado02.sharedkernel.domain.specification;

public abstract class CompositeSpecification<T> implements Specification<T> {
    @Override
    public abstract boolean isSatisfiedBy(T candidate);
}