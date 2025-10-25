package com.marcusprado02.sharedkernel.crosscutting.helpers.core;

public final class Trys {
  private Trys() {}
  public static <T> Result<T,Throwable> run(ThrowingSupplier<T> s) {
    try { return Result.ok(s.get()); } catch (Throwable t) { return Result.err(t); }
  }
  @FunctionalInterface public interface ThrowingSupplier<T> { T get() throws Exception; }
}

