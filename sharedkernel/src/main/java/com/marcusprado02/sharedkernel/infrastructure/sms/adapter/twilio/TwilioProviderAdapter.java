// src/main/java/com/marcusprado02/sharedkernel/infrastructure/sms/adapter/twilio/TwilioProviderAdapter.java
package com.marcusprado02.sharedkernel.infrastructure.sms.adapter.twilio;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.marcusprado02.sharedkernel.infrastructure.sms.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.sms.core.BaseProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.sms.model.*;
import com.marcusprado02.sharedkernel.infrastructure.sms.spi.Capabilities;
import com.marcusprado02.sharedkernel.infrastructure.sms.spi.ProviderConfig;
import com.marcusprado02.sharedkernel.infrastructure.sms.spi.ProviderMetadata;
import com.marcusprado02.sharedkernel.infrastructure.sms.spi.SignatureResolver;

public class TwilioProviderAdapter extends BaseProviderAdapter {

    private final ProviderConfig cfg;
    private static final String ID = "twilio";

    public TwilioProviderAdapter(ProviderConfig cfg) { this.cfg = cfg; }

    @Override public ProviderMetadata metadata() {
        return new ProviderMetadata(ID, "Twilio", "1.0",
                Set.of("US","BR","EU"),
                new Capabilities(true, true, true, true));
    }

    @Override public SmsResponse send(SmsRequest req, Policy policy) {
        // Força o alvo da lambda como Callable<T> para evitar inferência errada
        return run(policy, (Callable<SmsResponse>) () -> {
            // mapear req -> API Twilio (From, To, Body, StatusCallback)
            // respeitar Segmenter.detectEncoding e limitar tamanho/concatenação
            String msgId = "SM" + Instant.now().toEpochMilli();
            // ok(req, providerId, messageId, Map<String,Object>)
            return ok(req, ID, msgId,
                    Map.<String,Object>of(
                        "to", req.to().e164(),
                        "provider", ID
                    ));
        });
    }

    @Override public List<SmsResponse> sendBulk(BulkRequest req, Policy policy) {
        return req.messages().stream().map(m -> send(m, policy)).toList();
    }

    @Override public DeliveryReport getDeliveryReport(String messageId, SmsContext ctx) {
        return new DeliveryReport(ID, messageId, SmsStatus.DELIVERED, "000", "DELIVERED",
                Instant.now(), Map.of());
    }

    @Override public WebhookResult handleWebhook(String body, String signatureHeader, SignatureResolver resolver) {
        // Verificar assinatura do webhook (X-Twilio-Signature) com secret
        boolean valid = true; // TODO: validar de fato
        return new WebhookResult(valid, valid ? "accepted" : "invalid-signature");
    }
}
