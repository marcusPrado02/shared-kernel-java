package com.marcusprado02.sharedkernel.infrastructure.maptile.spi;

public interface ProviderConfigResolver {
    ProviderConfig resolve(String providerId);
}
