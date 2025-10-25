package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.adapter.spring;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.FeatureFlagClient;

@Aspect
public class FlaggedAspect {
  private final FeatureFlagClient client;
  public FlaggedAspect(FeatureFlagClient c){ this.client = c; }

  @Around("@annotation(flagged)")
  public Object around(ProceedingJoinPoint pjp, Flagged flagged) throws Throwable {
    var enabled = client.bool(flagged.key(), false, FlagContextFactory.fromSecurityContext());
    if (enabled) return pjp.proceed();
    if (!flagged.fallbackMethod().isBlank()) {
      var m = Arrays.stream(pjp.getTarget().getClass().getMethods())
                    .filter(mm -> mm.getName().equals(flagged.fallbackMethod()))
                    .findFirst().orElseThrow();
      return m.invoke(pjp.getTarget(), pjp.getArgs());
    }
    throw new IllegalStateException("Feature disabled: " + flagged.key());
  }
}
