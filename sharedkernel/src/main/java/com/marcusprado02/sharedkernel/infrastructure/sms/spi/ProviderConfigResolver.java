package com.marcusprado02.sharedkernel.infrastructure.sms.spi;


public interface ProviderConfigResolver {
    ProviderConfig resolve(String providerId);
}
