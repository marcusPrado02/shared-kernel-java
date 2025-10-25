package com.marcusprado02.sharedkernel.domain.policy;

import java.util.List;
import static com.marcusprado02.sharedkernel.domain.policy.Decision.*;

public final class Policies {
  private Policies(){}

  /** Deny-Overrides: qualquer DENY domina; se houver algum PERMIT e nenhum DENY → PERMIT; senão NOT_APPLICABLE/INDETERMINATE. */
  public static Policy denyOverrides(String id, String version, List<Policy> children) {
    return new Policy() {
      public String id(){ return id; }
      public String version(){ return version; }
      public PolicyResult evaluate(EvalContext ctx) {
        boolean sawPermit=false; PolicyResult lastNa=null; PolicyResult lastInd=null;
        for (var p: children) {
          var r = p.evaluate(ctx);
          if (r.decision==DENY) return r;
          if (r.decision==PERMIT) sawPermit=true;
          if (r.decision==NOT_APPLICABLE) lastNa=r;
          if (r.decision==INDETERMINATE) lastInd=r;
        }
        if (sawPermit) return PolicyResult.of(PERMIT, id, version, "permit-overrides-no-deny");
        if (lastInd!=null) return lastInd;
        return lastNa!=null? lastNa : PolicyResult.of(NOT_APPLICABLE, id, version, "no-policy-applicable");
      }
    };
  }

  /** Permit-Overrides: qualquer PERMIT domina (útil para “liberar exceções”) */
  public static Policy permitOverrides(String id, String version, List<Policy> children) {
    return new Policy() {
      public String id(){ return id; }
      public String version(){ return version; }
      public PolicyResult evaluate(EvalContext ctx) {
        PolicyResult lastNa=null; PolicyResult lastInd=null;
        for (var p: children) {
          var r = p.evaluate(ctx);
          if (r.decision==Decision.PERMIT) return r;
          if (r.decision==Decision.NOT_APPLICABLE) lastNa=r;
          if (r.decision==Decision.INDETERMINATE) lastInd=r;
        }
        if (lastInd!=null) return lastInd;
        return lastNa!=null? lastNa : PolicyResult.of(Decision.DENY, id, version, "default-deny");
      }
    };
  }

  /** First-Applicable: retorna o resultado do primeiro filho que não for NOT_APPLICABLE. */
  public static Policy firstApplicable(String id, String version, List<Policy> children) {
    return new Policy() {
      public String id(){ return id; }
      public String version(){ return version; }
      public PolicyResult evaluate(EvalContext ctx) {
        for (var p: children) {
          var r = p.evaluate(ctx);
          if (r.decision!=Decision.NOT_APPLICABLE) return r;
        }
        return PolicyResult.of(Decision.NOT_APPLICABLE, id, version, "no-first-applicable");
      }
    };
  }
}