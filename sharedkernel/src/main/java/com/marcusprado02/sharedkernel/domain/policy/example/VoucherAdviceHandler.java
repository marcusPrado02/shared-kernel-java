package com.marcusprado02.sharedkernel.domain.policy.example;

import com.marcusprado02.sharedkernel.domain.policy.Advice;
import com.marcusprado02.sharedkernel.domain.policy.EvalContext;
import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;
import com.marcusprado02.sharedkernel.domain.policy.enforcement.AdviceHandler;

public final class VoucherAdviceHandler implements AdviceHandler {
  @Override public boolean supports(Advice a) { return "OfferVoucher".equals(a.id()); }
  @Override public void handle(Advice a, EvalContext ctx, PolicyResult r) {
    // Pode anexar cabeçalhos ou meter “hints” no response context (em adapters REST/GraphQL)
    System.out.println("ADVICE voucher: " + a.params());
  }
}
