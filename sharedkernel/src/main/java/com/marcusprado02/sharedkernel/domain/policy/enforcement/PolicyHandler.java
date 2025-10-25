package com.marcusprado02.sharedkernel.domain.policy.enforcement;

import java.util.function.Supplier;

import com.marcusprado02.sharedkernel.domain.policy.EvalContext;

public interface PolicyHandler {

  /** Enforce ao redor de uma ação que retorna resultado (pre + obligations/advice + post-filter). */
  <T> T enforce(String policyId, String version, EvalContext ctx,
                Supplier<T> action,
                ResponseFilter<T> responseFilter) throws PolicyDeniedException;

  /** Enforce para ações sem retorno. */
  default void enforce(String policyId, String version, EvalContext ctx, Runnable action)
      throws PolicyDeniedException {
    enforce(policyId, version, ctx, () -> { action.run(); return null; }, ResponseFilter.noop());
  }
}
