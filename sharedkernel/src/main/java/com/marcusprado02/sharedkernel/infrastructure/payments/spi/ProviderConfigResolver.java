package com.marcusprado02.sharedkernel.infrastructure.payments.spi;

public interface ProviderConfigResolver {
    ProviderConfig resolve(String providerId);
}