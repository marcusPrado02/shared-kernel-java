package com.marcusprado02.sharedkernel.domain.policy.enforcement;

import com.marcusprado02.sharedkernel.domain.policy.Decision;
import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;

@FunctionalInterface
public interface DecisionMapper {
  void onDecision(PolicyResult result) throws PolicyDeniedException;

  static DecisionMapper defaultMapper() {
    return result -> {
      if (result.decision == Decision.DENY) throw new PolicyDeniedException(
        result.policyId(), result.policyVersion(), "DENY", result.reason);
      // INDETERMINATE/NOT_APPLICABLE: política do app decide; por padrão permitir.
    };
  }
}