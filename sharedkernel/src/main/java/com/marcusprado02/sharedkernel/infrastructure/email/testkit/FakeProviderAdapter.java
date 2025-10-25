package com.marcusprado02.sharedkernel.infrastructure.email.testkit;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.marcusprado02.sharedkernel.infrastructure.email.api.Capabilities;
import com.marcusprado02.sharedkernel.infrastructure.email.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.email.core.BaseProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.email.model.*;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.ProviderMetadata;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.SignatureResolver;

public class FakeProviderAdapter extends BaseProviderAdapter {
    @Override public ProviderMetadata metadata() {
        return new ProviderMetadata("fake-mail", "Fake Mail", "1.0",
                Set.of("GLOBAL"),
                new Capabilities(true,true,true,true,true));
    }
    @Override public EmailResponse send(EmailRequest req, Policy p) {
        if ("fail".equals(req.metadata().getOrDefault("mode","ok")))
            return failed("fake-mail", EmailErrorCode.CONTENT_REJECTED, Map.of());
        return ok("fake-mail", "FAKE-"+System.nanoTime(), Map.of());
    }
    @Override public List<EmailResponse> sendBulk(BulkRequest req, Policy p) {
        return req.messages().stream().map(r -> send(r, p)).toList();
    }
    @Override public EmailEvent getStatus(String messageId, EmailContext ctx) {
        return new EmailEvent("fake-mail", messageId, EmailStatus.DELIVERED, "delivered", null, java.time.Instant.now(), Map.of());
    }
    @Override public WebhookResult handleWebhook(String body, String signatureHeader, SignatureResolver resolver) {
        return new WebhookResult(true, "accepted");
    }
    @Override public InboundResult handleInbound(byte[] rawMime) {
        return new InboundResult(true, "accepted");
    }
}

