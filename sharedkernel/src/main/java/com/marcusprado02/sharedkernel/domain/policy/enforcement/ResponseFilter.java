package com.marcusprado02.sharedkernel.domain.policy.enforcement;

import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;

@FunctionalInterface
public interface ResponseFilter<T> {
  T apply(T response, PolicyResult decision);
  static <T> ResponseFilter<T> noop() { return (r, d) -> r; }
}