package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api;

import java.util.Optional;

public interface ConfigSource {
  Optional<ConfigSnapshot> load(); // conteúdo + version/hash
  String id();
}