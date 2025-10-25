package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api;

public interface ConfigValidator<T> {
  ValidationResult validate(T candidate);
}