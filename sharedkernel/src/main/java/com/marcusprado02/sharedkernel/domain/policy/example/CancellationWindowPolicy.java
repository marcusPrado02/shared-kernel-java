package com.marcusprado02.sharedkernel.domain.policy.example;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.marcusprado02.sharedkernel.domain.policy.Advice;
import com.marcusprado02.sharedkernel.domain.policy.Decision;
import com.marcusprado02.sharedkernel.domain.policy.EvalContext;
import com.marcusprado02.sharedkernel.domain.policy.Policy;
import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;
import com.marcusprado02.sharedkernel.domain.service.time.ClockProvider;

public final class CancellationWindowPolicy implements Policy {
  private final String id; private final String version;
  private final ClockProvider clock;
  private final Duration paidWindow;
  public CancellationWindowPolicy(String id, String version, ClockProvider clock, Duration paidWindow) {
    this.id=id; this.version=version; this.clock=clock; this.paidWindow=paidWindow;
  }
  @Override public String id(){ return id; }
  @Override public String version(){ return version; }

  @Override public PolicyResult evaluate(EvalContext ctx) {
    var status = ctx.get("order.status", String.class).orElse("UNKNOWN");
    if (status.equals("DRAFT") || status.equals("CONFIRMED"))
      return PolicyResult.of(Decision.PERMIT, id, version, "draft/confirmed");

    if (status.equals("PAID")) {
      var confirmedAt = ctx.get("order.confirmedAt", Instant.class).orElse(null);
      var shippedAt   = ctx.get("order.shippedAt", Instant.class).orElse(null);
      if (shippedAt!=null) return PolicyResult.of(Decision.DENY, id, version, "paid-but-shipped");
      if (confirmedAt==null) return PolicyResult.of(Decision.INDETERMINATE, id, version, "missing-confirmedAt");
      var age = Duration.between(confirmedAt, clock.now());
      if (age.compareTo(paidWindow) <= 0)
        return PolicyResult.of(Decision.PERMIT, id, version, "paid-within-window");
      return PolicyResult.of(Decision.DENY, id, version, "paid-window-expired")
             .withAdvice(java.util.List.of(new Advice("OfferVoucher", Map.of("value","5%","reason","window-expired"))));
    }

    if (status.equals("SHIPPED"))
      return PolicyResult.of(Decision.DENY, id, version, "already-shipped");

    return PolicyResult.of(Decision.NOT_APPLICABLE, id, version, "unknown-status");
  }
}

