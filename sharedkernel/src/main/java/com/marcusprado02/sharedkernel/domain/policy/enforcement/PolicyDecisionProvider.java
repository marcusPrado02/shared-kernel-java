package com.marcusprado02.sharedkernel.domain.policy.enforcement;

import com.marcusprado02.sharedkernel.domain.policy.EvalContext;
import com.marcusprado02.sharedkernel.domain.policy.Policy;
import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;

public interface PolicyDecisionProvider {
  PolicyResult evaluate(Policy policy, EvalContext ctx);
}
