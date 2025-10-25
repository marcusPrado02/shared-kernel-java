package com.marcusprado02.sharedkernel.infrastructure.email.spi;


public interface ProviderConfigResolver {
    ProviderConfig resolve(String providerId);
}