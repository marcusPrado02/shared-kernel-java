package com.marcusprado02.sharedkernel.crosscutting.parser.core;

// core/ParseResult.java
public sealed interface ParseResult<T> permits ParseResult.Ok, ParseResult.Err {
    record Ok<T>(T value) implements ParseResult<T> {}
    record Err<T>(ParseError error) implements ParseResult<T> {}

    static <T> Ok<T> ok(T v) { return new Ok<>(v); }
    static <T> Err<T> err(ParseError e) { return new Err<>(e); }

    default boolean isOk() { return this instanceof Ok<T>; }
    default T get() { return switch (this) { case Ok<T> o -> o.value; case Err<T> e -> throw new IllegalStateException(e.error().message()); }; }
    default <U> ParseResult<U> map(java.util.function.Function<T,U> f) {
        return switch (this) { case Ok<T> o -> ok(f.apply(o.value)); case Err<T> e -> err(e.error()); };
    }
    default <U> ParseResult<U> flatMap(java.util.function.Function<T,Parser<U>> f) {
        return switch (this) { case Ok<T> o -> f.apply(o.value).parse(o.value.toString()); case Err<T> e -> err(e.error()); };
    }
}
