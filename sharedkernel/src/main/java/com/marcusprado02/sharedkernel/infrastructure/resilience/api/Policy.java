package com.marcusprado02.sharedkernel.infrastructure.resilience.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Policy {
  PolicyKey key();
  <T> T execute(ExecutionContext ctx, CheckedSupplier<T> supplier) throws Exception;
  <T> CompletableFuture<T> executeAsync(ExecutionContext ctx, Supplier<CompletableFuture<T>> supplier);
}
