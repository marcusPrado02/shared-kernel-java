package com.marcusprado02.sharedkernel.infrastructure.config.validation.api;

import java.util.List;

public interface ConfigValidator<T> {
  ValidationResult validate(T candidate);
  static <T> ConfigValidator<T> composite(List<ConfigValidator<T>> validators) {
    return candidate -> validators.stream()
      .map(v -> v.validate(candidate))
      .reduce(ValidationResult.ok(), ValidationResult::merge);
  }
}

