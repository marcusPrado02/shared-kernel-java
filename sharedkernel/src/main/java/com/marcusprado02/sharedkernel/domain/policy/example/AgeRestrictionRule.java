package com.marcusprado02.sharedkernel.domain.policy.example;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

import com.marcusprado02.sharedkernel.domain.policy.Decision;
import com.marcusprado02.sharedkernel.domain.policy.EvalContext;
import com.marcusprado02.sharedkernel.domain.policy.Policy;
import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;

public final class AgeRestrictionRule implements Policy {
  private final String id; private final String version;
  private final Map<String,Integer> minAgeByCategory; // ex: {"ALCOHOL":18, "ADULT":18}
  public AgeRestrictionRule(String id, String version, Map<String,Integer> minAgeByCategory) {
    this.id=id; this.version=version; this.minAgeByCategory = Map.copyOf(minAgeByCategory);
  }
  public String id(){ return id; }
  public String version(){ return version; }

  @Override public PolicyResult evaluate(EvalContext ctx) {
    var category = ctx.get("item.category", String.class).orElse(null);
    if (category==null || !minAgeByCategory.containsKey(category))
      return PolicyResult.of(Decision.NOT_APPLICABLE, id, version, "no-category/na");
    var dob = ctx.get("subject.dob", LocalDate.class).orElse(null);
    if (dob==null) return PolicyResult.of(Decision.INDETERMINATE, id, version, "missing-dob");
    var age = Period.between(dob, LocalDate.now()).getYears();
    var required = minAgeByCategory.get(category);
    var ok = age >= required;
    return PolicyResult.of(ok? Decision.PERMIT : Decision.DENY, id, version,
            ok? "age-ok" : "age-"+age+"-lt-required-"+required);
  }
}
