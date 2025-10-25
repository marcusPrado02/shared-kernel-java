package com.marcusprado02.sharedkernel.infrastructure.email.spi;

public interface ProviderFactory {
    String providerId();
    ProviderAdapter create(ProviderConfig cfg);
}
