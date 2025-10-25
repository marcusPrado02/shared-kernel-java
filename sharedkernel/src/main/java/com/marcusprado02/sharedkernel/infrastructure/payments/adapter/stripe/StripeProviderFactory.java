package com.marcusprado02.sharedkernel.infrastructure.payments.adapter.stripe;

import com.marcusprado02.sharedkernel.infrastructure.payments.spi.ProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.payments.spi.ProviderFactory;
import com.marcusprado02.sharedkernel.infrastructure.payments.spi.ProviderConfig;

class StripeProviderFactory implements ProviderFactory {
    @Override public String providerId() { return "stripe"; }
    @Override public ProviderAdapter create(ProviderConfig cfg) { return new StripeProviderAdapter(cfg); }
}
