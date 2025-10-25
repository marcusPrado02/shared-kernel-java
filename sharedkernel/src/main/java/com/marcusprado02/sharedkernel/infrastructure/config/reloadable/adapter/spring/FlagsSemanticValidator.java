package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Bucket;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Rule;
import com.marcusprado02.sharedkernel.infrastructure.config.validation.api.ConfigValidator;
import com.marcusprado02.sharedkernel.infrastructure.config.validation.api.ValidationResult;

public final class FlagsSemanticValidator implements ConfigValidator<Map<String, FlagDefinition>> {
  @Override
  public ValidationResult validate(Map<String, FlagDefinition> defs) {
    List<String> errors = new ArrayList<>();
    defs.forEach((k, d) -> {
      if (d.variants() == null || d.variants().isEmpty()) {
        errors.add("Flag " + k + " has no variants");
      }
      if (d.rules() != null) {
        for (Rule r : d.rules()) {
          if (r.rollout() != null && r.rollout().buckets() != null) {
            int sum = r.rollout().buckets().stream().mapToInt(Bucket::weight).sum();
            if (sum != 10_000) errors.add("Flag " + k + " rule " + r.id() + " buckets sum != 10000");
            for (Bucket b : r.rollout().buckets()) {
              if (!d.variants().containsKey(b.variant())) {
                errors.add("Flag " + k + " rule " + r.id() + " references missing variant: " + b.variant());
              }
            }
          }
        }
      }
    });
    return errors.isEmpty() ? ValidationResult.ok() : new ValidationResult(false, errors, Map.of());
  }
}