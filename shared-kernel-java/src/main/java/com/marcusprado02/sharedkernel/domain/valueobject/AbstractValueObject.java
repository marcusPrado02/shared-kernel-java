package com.marcusprado02.sharedkernel.domain.valueobject;

public abstract class AbstractValueObject<T> implements ValueObject {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return equalsCore((T) o);
    }

    @Override
    public int hashCode() {
        return hashCodeCore();
    }

    /**
     * Compare todos os campos relevantes.
     */
    protected abstract boolean equalsCore(T other);

    /**
     * Retorne Objects.hash(…) com TODOS os campos usados em equalsCore.
     */
    protected abstract int hashCodeCore();
}

