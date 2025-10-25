package com.marcusprado02.sharedkernel.infrastructure.cache;

import java.util.Optional;

/** Estratégia de chave composta (tenant, namespace, versão, hash). */
public interface CacheKeyStrategy {
  String build(String namespace, String tenantId, String rawKey, Optional<String> version);
}

