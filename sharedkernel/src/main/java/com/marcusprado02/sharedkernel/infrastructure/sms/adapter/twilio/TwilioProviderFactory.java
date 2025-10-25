package com.marcusprado02.sharedkernel.infrastructure.sms.adapter.twilio;

import com.marcusprado02.sharedkernel.infrastructure.sms.spi.ProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.sms.spi.ProviderConfig;
import com.marcusprado02.sharedkernel.infrastructure.sms.spi.ProviderFactory;

class TwilioProviderFactory implements ProviderFactory {
    @Override public String providerId() { return "twilio"; }
    @Override public ProviderAdapter create(ProviderConfig config) { return new TwilioProviderAdapter(config); }
}
