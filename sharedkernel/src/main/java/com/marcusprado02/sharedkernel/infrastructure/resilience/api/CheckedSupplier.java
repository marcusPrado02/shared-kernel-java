package com.marcusprado02.sharedkernel.infrastructure.resilience.api;


import java.util.function.Supplier;

@FunctionalInterface
public interface CheckedSupplier<T> {
  T get() throws Exception;

  /** Converte para Supplier “unchecked” (propaga checked como runtime). */
  static <T> Supplier<T> unchecked(CheckedSupplier<T> cs) {
    return () -> {
      try { return cs.get(); }
      catch (Exception e) { return sneakyThrow(e); }
    };
  }

  @SuppressWarnings("unchecked")
  static <R, E extends Throwable> R sneakyThrow(Throwable t) throws E {
    throw (E) t;
  }
}

