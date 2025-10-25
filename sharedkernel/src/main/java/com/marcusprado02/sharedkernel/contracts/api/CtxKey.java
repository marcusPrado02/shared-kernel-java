package com.marcusprado02.sharedkernel.contracts.api;

/** Chave tipada para o contexto. */
public record CtxKey<T>(String name, Class<T> type) {
    public static <T> CtxKey<T> of(String name, Class<T> type) { return new CtxKey<>(name, type); }
}
