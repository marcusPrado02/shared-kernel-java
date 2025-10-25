package com.marcusprado02.sharedkernel.infrastructure.maptile.spi;

public interface ProviderFactory {
    String providerId();
    ProviderAdapter create(ProviderConfig cfg);
}
