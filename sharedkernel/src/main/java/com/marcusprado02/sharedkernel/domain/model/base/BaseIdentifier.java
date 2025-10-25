package com.marcusprado02.sharedkernel.domain.model.base;

import java.util.Objects;

/** Base para IDs fortes tipados. Imutável e com igualdade por valor. */
public abstract class BaseIdentifier<T> implements Identifier {
    private final T value;

    protected BaseIdentifier(T value) {
        this.value = Objects.requireNonNull(value, "id value must not be null");
    }

    public T value() { return value; }

    @Override public String asString() { return String.valueOf(value); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || effectiveClass() != getEffectiveClass(o)) return false;
        var that = (BaseIdentifier<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override public int hashCode() { return Objects.hash(effectiveClass(), value); }

    @Override public String toString() { return asString(); }

    /** Permite adaptar para frameworks com proxies (override em subclasses se necessário). */
    protected Class<?> effectiveClass() { return getClass(); }

    private static Class<?> getEffectiveClass(Object o) { return o.getClass(); }
}
