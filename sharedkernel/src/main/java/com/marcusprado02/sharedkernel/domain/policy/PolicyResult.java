package com.marcusprado02.sharedkernel.domain.policy;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PolicyResult {
  public final Decision decision;
  public final String policyId;
  public final String policyVersion;
  public final String reason;            // humano / explicável
  public final List<Obligation> obligations;
  public final List<Advice> advice;
  public final Map<String,String> attributes; // echo/diagnóstico
  public final UUID correlationId;
  public final Instant evaluatedAt;

  private PolicyResult(Decision d, String id, String ver, String reason,
                       List<Obligation> obs, List<Advice> adv, Map<String,String> attrs,
                       UUID corr, Instant ts) {
    this.decision = d; this.policyId = id; this.policyVersion = ver; this.reason = reason;
    this.obligations = obs; this.advice = adv; this.attributes = attrs;
    this.correlationId = corr; this.evaluatedAt = ts;
  }
  public static PolicyResult of(Decision d, String id, String ver, String reason) {
    return new PolicyResult(d, id, ver, reason, List.of(), List.of(), Map.of(), UUID.randomUUID(), java.time.Instant.now());
  }
  public PolicyResult withObligations(List<Obligation> o){ return new PolicyResult(decision,policyId,policyVersion,reason,o,advice,attributes,correlationId,evaluatedAt); }
  public PolicyResult withAdvice(List<Advice> a){ return new PolicyResult(decision,policyId,policyVersion,reason,obligations,a,attributes,correlationId,evaluatedAt); }
  public PolicyResult withAttributes(Map<String,String> attrs){ return new PolicyResult(decision,policyId,policyVersion,reason,obligations,advice,attrs,correlationId,evaluatedAt); }
  public String policyId() {
    return policyId;
  }
  public String policyVersion() {
    return policyVersion;
  }
}