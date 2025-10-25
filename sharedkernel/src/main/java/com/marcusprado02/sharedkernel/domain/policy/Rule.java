package com.marcusprado02.sharedkernel.domain.policy;

@FunctionalInterface
public interface Rule {
  /** Pode retornar NOT_APPLICABLE quando a regra não se aplica ao contexto. */
  PolicyResult evaluate(EvalContext ctx);
}
