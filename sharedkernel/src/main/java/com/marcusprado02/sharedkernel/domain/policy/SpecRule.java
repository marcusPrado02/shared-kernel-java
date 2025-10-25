package com.marcusprado02.sharedkernel.domain.policy;

import com.marcusprado02.sharedkernel.domain.repository.Specification;

public final class SpecRule<T> implements Rule {
  private final String id, version; private final Specification<T> spec; private final String reason;
  public SpecRule(String id, String version, Specification<T> spec, String reason) {
    this.id=id; this.version=version; this.spec=spec; this.reason=reason;
  }
  @Override public PolicyResult evaluate(EvalContext ctx) {
    var obj = ctx.get("subjectObject", Object.class).orElse(null);
    if (obj==null) return PolicyResult.of(Decision.NOT_APPLICABLE, id, version, "no-subjectObject");
    @SuppressWarnings("unchecked") var t = (T) obj;
    var ok = spec.isSatisfiedBy(t);
    return PolicyResult.of(ok? Decision.PERMIT: Decision.DENY, id, version, ok? "spec-ok": reason);
  }
}
