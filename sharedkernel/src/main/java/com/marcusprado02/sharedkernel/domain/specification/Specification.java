package com.marcusprado02.sharedkernel.domain.specification;

import java.util.function.Predicate;

/**
 * Specification genérica.
 * - Pode ser usada como Predicate in-memory.
 * - Pode ser traduzida pelo Adapter para query DSL.
 */
@FunctionalInterface
public interface Specification<T> extends Predicate<T> {

    boolean isSatisfiedBy(T candidate);

    @Override
    default boolean test(T t) { return isSatisfiedBy(t); }

    default Specification<T> and(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    default Specification<T> or(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    default Specification<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }
}