package com.marcusprado02.sharedkernel.infrastructure.secrets;

import java.time.Duration;
import java.util.Optional;

// Cache pluggable; imple com Caffeine/Redis conforme necessidade
public interface SecretsCache {
    Optional<SecretValue> get(String key);
    void put(String key, SecretValue value, Duration ttl);
    void invalidate(String keyPrefix);
}
