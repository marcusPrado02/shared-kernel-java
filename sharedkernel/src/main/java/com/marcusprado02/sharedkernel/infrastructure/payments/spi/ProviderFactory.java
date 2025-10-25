package com.marcusprado02.sharedkernel.infrastructure.payments.spi;


public interface ProviderFactory {
    String providerId();
    ProviderAdapter create(ProviderConfig config);
}
