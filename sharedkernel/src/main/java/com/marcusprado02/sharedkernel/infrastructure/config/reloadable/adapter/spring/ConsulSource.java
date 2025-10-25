package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring;


import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSnapshot;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSource;

/**
 * Stub leve: em produção, integre HTTP KV do Consul.
 */
public final class ConsulSource implements ConfigSource {
  private final FlagsProperties.Consul consul;

  public ConsulSource(FlagsProperties.Consul consul) { this.consul = consul; }

  @Override public Optional<ConfigSnapshot> load() {
    String yaml = "version: \"0\"\nflags: {}\n";
    return Optional.of(new ConfigSnapshot("consul-0", yaml.getBytes(StandardCharsets.UTF_8), "application/x-yaml"));
  }

  @Override public String id() { return "consul:" + consul.getHost() + "/" + consul.getKey(); }
}
