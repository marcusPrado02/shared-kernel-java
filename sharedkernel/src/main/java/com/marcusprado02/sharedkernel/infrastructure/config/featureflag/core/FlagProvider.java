package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.core;

import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;

public interface FlagProvider {
  Optional<FlagDefinition> get(String key);
  Map<String, FlagDefinition> getAll(); // p/ pre-warm
  String providerName();
}   