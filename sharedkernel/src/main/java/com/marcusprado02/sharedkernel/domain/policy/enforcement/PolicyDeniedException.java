package com.marcusprado02.sharedkernel.domain.policy.enforcement;

public final class PolicyDeniedException extends RuntimeException {
  private final String policyId, policyVersion, reason, decision;
  public PolicyDeniedException(String policyId, String policyVersion, String decision, String reason) {
    super("policy denied: %s@%s (%s) - %s".formatted(policyId, policyVersion, decision, reason));
    this.policyId = policyId; this.policyVersion = policyVersion; this.reason = reason; this.decision = decision;
  }
  public String policyId(){ return policyId; }
  public String policyVersion(){ return policyVersion; }
  public String reason(){ return reason; }
  public String decision(){ return decision; }
}
