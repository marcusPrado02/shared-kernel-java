package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.provider.FlagYamlParser;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigParser;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSnapshot;

public final class FlagsConfigParser implements ConfigParser<Map<String, FlagDefinition>> {
  @Override
  public Map<String, FlagDefinition> parse(ConfigSnapshot snap) {
    if (snap == null || snap.content() == null || snap.content().length == 0) {
      return Map.of();
    }
    var reader = new InputStreamReader(new ByteArrayInputStream(snap.content()), StandardCharsets.UTF_8);
    return FlagYamlParser.parse(reader);
  }
}