package com.marcusprado02.sharedkernel.domain.policy.example;

import com.marcusprado02.sharedkernel.domain.policy.EvalContext;
import com.marcusprado02.sharedkernel.domain.policy.Obligation;
import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;
import com.marcusprado02.sharedkernel.domain.policy.enforcement.ObligationHandler;

public final class AuditObligationHandler implements ObligationHandler {
  @Override public boolean supports(Obligation o) { return "AuditTrail".equals(o.id()); }
  @Override public void handle(Obligation o, EvalContext ctx, PolicyResult r) {
    // Envie para seu logger/OTEL (sem PII):
    System.out.printf("AUDIT policy=%s@%s tenant=%s decision=%s subject=%s resource=%s reason=%s%n",
      r.policyId(), r.policyVersion(), ctx.tenant, r.decision, ctx.subject, ctx.resource, r.reason);
  }
}
