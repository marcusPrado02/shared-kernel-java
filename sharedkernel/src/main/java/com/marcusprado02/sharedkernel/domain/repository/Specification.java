package com.marcusprado02.sharedkernel.domain.repository;

@FunctionalInterface
public interface Specification<T> {
    boolean isSatisfiedBy(T aggregate); // útil para testes in-memory

    /** dica: adapters podem traduzir para SQL/Criteria/DSL */
    default Specification<T> and(Specification<T> other) {
        return t -> this.isSatisfiedBy(t) && other.isSatisfiedBy(t);
    }
    default Specification<T> or(Specification<T> other) {
        return t -> this.isSatisfiedBy(t) || other.isSatisfiedBy(t);
    }
    static <T> Specification<T> alwaysTrue() { return t -> true; }
}
