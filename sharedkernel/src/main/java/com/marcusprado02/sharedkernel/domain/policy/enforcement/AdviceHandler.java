package com.marcusprado02.sharedkernel.domain.policy.enforcement;

import com.marcusprado02.sharedkernel.domain.policy.Advice;
import com.marcusprado02.sharedkernel.domain.policy.EvalContext;
import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;

public interface AdviceHandler {
  boolean supports(Advice a);
  void handle(Advice a, EvalContext ctx, PolicyResult result);
}
