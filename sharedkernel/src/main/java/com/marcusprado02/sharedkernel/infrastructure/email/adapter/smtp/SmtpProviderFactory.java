package com.marcusprado02.sharedkernel.infrastructure.email.adapter.smtp;

import com.marcusprado02.sharedkernel.infrastructure.email.spi.ProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.ProviderConfig;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.ProviderFactory;

class SmtpProviderFactory implements ProviderFactory {
    @Override public String providerId() { return "smtp"; }
    @Override public ProviderAdapter create(ProviderConfig cfg) { return new SmtpProviderAdapter(cfg); }
}
