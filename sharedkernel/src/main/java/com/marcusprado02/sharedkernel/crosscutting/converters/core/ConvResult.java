package com.marcusprado02.sharedkernel.crosscutting.converters.core;

public sealed interface ConvResult<T> permits ConvResult.Ok, ConvResult.Err {
    record Ok<T>(T value) implements ConvResult<T> {}
    record Err<T>(String message, Throwable cause) implements ConvResult<T> {}
    static <T> Ok<T> ok(T v) { return new Ok<>(v); }
    static <T> Err<T> err(String m, Throwable c) { return new Err<>(m,c); }
}

