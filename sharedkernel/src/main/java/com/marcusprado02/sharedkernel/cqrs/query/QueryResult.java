package com.marcusprado02.sharedkernel.cqrs.query;

import java.util.List;
import java.util.Optional;

/** Resultado padrão de Query (não paginada). Para paginação, use Page<T>. */
public record QueryResult<R>(Optional<R> value, List<String> warnings) {
    public static <T> QueryResult<T> of(T v){ return new QueryResult<>(Optional.ofNullable(v), List.of()); }
    public static <T> QueryResult<T> empty(){ return new QueryResult<>(Optional.empty(), List.of()); }
}