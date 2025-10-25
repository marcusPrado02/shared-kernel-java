package com.marcusprado02.sharedkernel.crosscutting.helpers.core;

public sealed interface Result<T,E> permits Result.Ok, Result.Err {
  record Ok<T,E>(T value) implements Result<T,E> {}
  record Err<T,E>(E error) implements Result<T,E> {}

  static <T,E> Ok<T,E> ok(T v) { return new Ok<>(v); }
  static <T,E> Err<T,E> err(E e) { return new Err<>(e); }
}
