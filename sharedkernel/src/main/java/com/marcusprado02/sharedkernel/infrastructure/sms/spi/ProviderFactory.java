package com.marcusprado02.sharedkernel.infrastructure.sms.spi;

public interface ProviderFactory {
    String providerId();
    ProviderAdapter create(ProviderConfig config);
}
