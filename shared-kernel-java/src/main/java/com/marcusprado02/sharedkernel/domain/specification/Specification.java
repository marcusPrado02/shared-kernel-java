package com.marcusprado02.sharedkernel.domain.specification;

import java.util.function.Predicate;

@FunctionalInterface
public interface Specification<T> {

    /**
     * Verifica se a entidade satisfaz a regra.
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * Combinação AND com outra especificação.
     */
    default Specification<T> and(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    /**
     * Combinação OR com outra especificação.
     */
    default Specification<T> or(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    /**
     * Negação lógica da especificação.
     */
    default Specification<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }

    /**
     * Converte a especificação para um `Predicate<T>` funcional.
     */
    default Predicate<T> toPredicate() {
        return this::isSatisfiedBy;
    }

    /**
     * Nomeia a especificação (útil para debug e logs).
     */
    default NamedSpecification<T> named(String name) {
        return new NamedSpecification<>(name, this);
    }

    /**
     * Especificação que sempre retorna true (default).
     */
    static <T> Specification<T> alwaysTrue() {
        return candidate -> true;
    }

    /**
     * Especificação que sempre retorna false.
     */
    static <T> Specification<T> alwaysFalse() {
        return candidate -> false;
    }
}
