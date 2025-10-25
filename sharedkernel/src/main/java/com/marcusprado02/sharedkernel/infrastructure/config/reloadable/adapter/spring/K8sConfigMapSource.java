package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring;


import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSnapshot;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSource;

/**
 * Stub leve: em produção, integre o client oficial do Kubernetes.
 * Aqui retornamos Optional.empty() para não quebrar, a não ser que você pluge conteúdo manualmente.
 */
public final class K8sConfigMapSource implements ConfigSource {
  private final FlagsProperties.K8s k8s;

  public K8sConfigMapSource(FlagsProperties.K8s k8s) { this.k8s = k8s; }

  @Override public Optional<ConfigSnapshot> load() {
    // TODO: implementar client K8s; placeholder seguro:
    String yaml = "version: \"0\"\nflags: {}\n";
    return Optional.of(new ConfigSnapshot("k8s-0", yaml.getBytes(StandardCharsets.UTF_8), "application/x-yaml"));
  }

  @Override public String id() { return "k8s:" + k8s.getNamespace() + "/" + k8s.getConfigMap() + "#" + k8s.getKey(); }
}
