package com.marcusprado02.sharedkernel.domain.model.value;


import java.util.Arrays;
import java.util.Objects;

/**
 * Base utilitária: subclasses implementam equalityComponents() e
 * ganham equals/hashCode/toString consistentes.
 */
public abstract class AbstractValueObject implements ValueObject {

    protected abstract Object[] equalityComponents();

    @Override public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != getClass()) return false;
        return Arrays.equals(equalityComponents(), ((AbstractValueObject) o).equalityComponents());
    }

    @Override public final int hashCode() {
        return Arrays.hashCode(equalityComponents());
    }

    @Override public String toString() {
        return getClass().getSimpleName() + Arrays.toString(equalityComponents());
    }

    protected static <T> T req(T v, String name) {
        return Objects.requireNonNull(v, name + " must not be null");
    }
}