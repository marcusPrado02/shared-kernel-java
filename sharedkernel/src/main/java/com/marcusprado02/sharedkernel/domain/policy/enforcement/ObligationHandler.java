package com.marcusprado02.sharedkernel.domain.policy.enforcement;

import java.util.List;

import com.marcusprado02.sharedkernel.domain.policy.EvalContext;
import com.marcusprado02.sharedkernel.domain.policy.Obligation;
import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;

public interface ObligationHandler {
  boolean supports(Obligation o);
  void handle(Obligation o, EvalContext ctx, PolicyResult result);
}
