package com.marcusprado02.sharedkernel.domain.service.result;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public sealed interface DomainResult<T,E extends Serializable>
        extends Serializable permits DomainResult.Ok, DomainResult.Err {

    record Ok<T,E extends Serializable>(T value) implements DomainResult<T,E> {}
    record Err<T,E extends Serializable>(E error) implements DomainResult<T,E> {}

    static <T,E extends Serializable> DomainResult<T,E> ok(T v)  { return new Ok<>(v); }
    static <T,E extends Serializable> DomainResult<T,E> err(E e) { return new Err<>(Objects.requireNonNull(e)); }

    default boolean isOk()  { return this instanceof Ok<?,?>; }
    default boolean isErr() { return this instanceof Err<?,?>; }

    default <U> DomainResult<U,E> map(Function<T,U> f) {
        return switch (this) {
            case Ok<T,E> o -> ok(f.apply(o.value()));
            case Err<T,E> e -> err(e.error());
        };
    }

    default <U> DomainResult<U,E> flatMap(Function<T,DomainResult<U,E>> f) {
        return switch (this) {
            case Ok<T,E> o -> Objects.requireNonNull(f.apply(o.value()));
            case Err<T,E> e -> err(e.error());
        };
    }

    default Optional<T> toOptional() {
        return this instanceof Ok<T,E> o ? Optional.of(o.value()) : Optional.empty();
    }
}