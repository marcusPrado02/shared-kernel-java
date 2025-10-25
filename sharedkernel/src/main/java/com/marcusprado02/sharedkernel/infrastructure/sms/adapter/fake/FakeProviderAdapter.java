// src/main/java/com/marcusprado02/sharedkernel/infrastructure/sms/adapter/fake/FakeProviderAdapter.java
package com.marcusprado02.sharedkernel.infrastructure.sms.adapter.fake;

import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.sms.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.sms.core.BaseProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.sms.model.*;
import com.marcusprado02.sharedkernel.infrastructure.sms.spi.*;

public class FakeProviderAdapter extends BaseProviderAdapter {
    @Override public ProviderMetadata metadata() {
        return new ProviderMetadata("fake", "FakeSMS", "1.0", java.util.Set.of("*"),
                new Capabilities(true, true, true, true));
    }

    @Override public SmsResponse send(SmsRequest req, Policy policy) {
        if ("fail".equals(req.metadata().getOrDefault("mode","ok"))) {
            // precisa do req e Map<String,Object>
            return failed(req, "fake", SmsErrorCode.CARRIER_BLOCKED, Map.<String,Object>of());
        }
        // precisa do req e Map<String,Object>
        return ok(req, "fake", "FAKE-" + System.nanoTime(), Map.<String,Object>of());
    }

    @Override public java.util.List<SmsResponse> sendBulk(BulkRequest req, Policy policy) {
        return req.messages().stream().map(m -> send(m, policy)).toList();
    }

    @Override public DeliveryReport getDeliveryReport(String messageId, SmsContext ctx) {
        return new DeliveryReport("fake", messageId, SmsStatus.DELIVERED, "000", "OK",
                java.time.Instant.now(), Map.of());
    }

    @Override public WebhookResult handleWebhook(String body, String signatureHeader, SignatureResolver resolver) {
        return new WebhookResult(true, "accepted");
    }
}
