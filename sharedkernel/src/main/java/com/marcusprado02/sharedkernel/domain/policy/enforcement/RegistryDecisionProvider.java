package com.marcusprado02.sharedkernel.domain.policy.enforcement;

import com.marcusprado02.sharedkernel.domain.policy.EvalContext;
import com.marcusprado02.sharedkernel.domain.policy.Policy;
import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;

public final class RegistryDecisionProvider implements PolicyDecisionProvider {
  @Override public PolicyResult evaluate(Policy policy, EvalContext ctx) {
    return policy.evaluate(ctx);
  }
}