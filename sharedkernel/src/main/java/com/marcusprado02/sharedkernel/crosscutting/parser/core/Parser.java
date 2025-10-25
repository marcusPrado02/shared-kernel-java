package com.marcusprado02.sharedkernel.crosscutting.parser.core;

import java.util.Optional;
import java.util.function.Function;

// core/Parser.java
@FunctionalInterface
public interface Parser<T> {
    ParseResult<T> parse(String input);
    default <U> Parser<U> map(Function<T,U> f) {
        return in -> this.parse(in).map(f);
    }
    default <U> Parser<U> flatMap(Function<T, Parser<U>> f) {
        return in -> this.parse(in).flatMap(f);
    }
    default Parser<T> orElse(Parser<T> fallback) {
        return in -> {
            var r = this.parse(in);
            return r.isOk() ? r : fallback.parse(in);
        };
    }
    default Parser<Optional<T>> optional() {
        return in -> {
            var r = this.parse(in);
            return r.isOk() ? ParseResult.ok(Optional.of(r.get()))
                            : ParseResult.ok(Optional.empty());
        };
    }
}

