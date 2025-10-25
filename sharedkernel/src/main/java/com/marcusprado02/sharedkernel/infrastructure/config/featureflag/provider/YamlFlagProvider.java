package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.provider;

import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.core.FlagProvider;

public final class YamlFlagProvider implements FlagProvider {
  private final Map<String, FlagDefinition> defs;
  public YamlFlagProvider(Reader yamlReader) { this.defs = FlagYamlParser.parse(yamlReader); }
  public Optional<FlagDefinition> get(String key){ return Optional.ofNullable(defs.get(key)); }
  public Map<String, FlagDefinition> getAll(){ return Collections.unmodifiableMap(defs); }
  public String providerName(){ return "yaml"; }
}