// src/main/java/com/marcusprado02/sharedkernel/infrastructure/email/core/DefaultEmailGateway.java
package com.marcusprado02.sharedkernel.infrastructure.email.core;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.events.domain.DomainEvent;
import com.marcusprado02.sharedkernel.events.domain.GenericDomainEvent;
import com.marcusprado02.sharedkernel.infrastructure.email.api.Capabilities;
import com.marcusprado02.sharedkernel.infrastructure.email.api.EmailGateway;
import com.marcusprado02.sharedkernel.infrastructure.email.api.EmailRouter;
import com.marcusprado02.sharedkernel.infrastructure.email.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.email.api.TemplateRenderer;
import com.marcusprado02.sharedkernel.infrastructure.email.model.*;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.ProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.ProviderFactory;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.ProviderConfig;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.ProviderConfigResolver;

// Outbox (contratos genéricos do seu outbox)
import com.marcusprado02.sharedkernel.infrastructure.outbox.OutboxPublisher;

public class DefaultEmailGateway implements EmailGateway {

    private final Map<String, ProviderAdapter> providers = new HashMap<>();
    private final EmailRouter router;
    private final IdempotencyStore idem;
    private final OutboxPublisher outbox;
    private final SuppressionList suppression;
    private final SignatureRegistry signatures;
    private final TemplateRenderer renderer;
    private final Map<String, Capabilities> caps = new HashMap<>();
    private final Duration dedupeTtl = Duration.ofMinutes(30);

    public DefaultEmailGateway(Collection<ProviderFactory> factories,
                               ProviderConfigResolver cfgResolver,
                               EmailRouter router,
                               IdempotencyStore idem,
                               OutboxPublisher outbox,
                               SuppressionList suppression,
                               SignatureRegistry signatures,
                               TemplateRenderer renderer) {
        this.router = router; this.idem = idem; this.outbox = outbox;
        this.suppression = suppression; this.signatures = signatures; this.renderer = renderer;

        for (ProviderFactory f : factories) {
            ProviderConfig cfg = cfgResolver.resolve(f.providerId());
            ProviderAdapter p = f.create(cfg);
            providers.put(f.providerId(), p);
            caps.put(f.providerId(), p.metadata().capabilities());
        }
    }

    private ProviderAdapter pick(EmailContext ctx){
        String id = Optional.ofNullable(ctx.preferredProvider()).orElseGet(() -> router.route(ctx));
        ProviderAdapter p = providers.get(id);
        if (p == null) throw new IllegalArgumentException("Provider não encontrado: " + id);
        return p;
    }

    private void enforceCompliance(EmailRequest req){
        for (Address a : req.to()) {
            if (suppression.isSuppressed(req.context().tenantId(), a.email())) {
                throw new IllegalStateException("Destinatário suprimido: " + a.email());
            }
        }
        // Aqui você pode reforçar: List-Unsubscribe, physical address, categoria/campaignId, etc.
    }

    private EmailRequest renderIfNeeded(EmailRequest req){
        return renderer.render(req);
    }

    @Override
    public EmailResponse send(EmailRequest rawReq, Policy policy) {
        enforceCompliance(rawReq);
        EmailRequest req = renderIfNeeded(rawReq);
        return idem.find(req.idempotencyKey()).orElseGet(() -> {
            EmailResponse res = pick(req.context()).send(req, policy);
            idem.save(req.idempotencyKey(), res, dedupeTtl);
            if (policy.outboxEnabled()) {
                DomainEvent evt = new GenericDomainEvent(
                        "EMAIL.SENT",
                        res.messageId(),
                        res,
                        Instant.now(),
                        Map.of("providerId", res.providerId())
                );
                outbox.publish(List.of(evt));
            }
            return res;
        });
    }

    @Override
    public List<EmailResponse> sendBulk(BulkRequest bulk, Policy policy) {
        List<EmailRequest> ready = new ArrayList<>(bulk.messages().size());
        for (EmailRequest r : bulk.messages()) { enforceCompliance(r); ready.add(renderIfNeeded(r)); }
        List<EmailResponse> res = pick(ready.get(0).context()).sendBulk(new BulkRequest(ready), policy);
        if (policy.outboxEnabled()) {
            List<DomainEvent> events = new ArrayList<>(res.size());
            for (EmailResponse x : res) {
                events.add(new GenericDomainEvent(
                        "EMAIL.SENT",
                        x.messageId(),
                        x,
                        Instant.now(),
                        Map.of("providerId", x.providerId())
                ));
            }
            outbox.publish(events);
        }
        return res;
    }

    @Override public CompletableFuture<EmailResponse> sendAsync(EmailRequest req, Policy p){
        return pick(req.context()).sendAsync(renderIfNeeded(req), p);
    }

    @Override public CompletableFuture<List<EmailResponse>> sendBulkAsync(BulkRequest req, Policy p){
        return CompletableFuture.supplyAsync(() -> sendBulk(req, p));
    }

    @Override
    public EmailEvent getStatus(String providerId, String messageId, EmailContext ctx) {
        ProviderAdapter p = providers.get(providerId);
        if (p == null) throw new IllegalArgumentException("Provider desconhecido");
        return p.getStatus(messageId, ctx);
    }

    // >>> Implementação que faltava <<<
    @Override
    public List<EmailEvent> getStatusBulk(String providerId, List<String> messageIds, EmailContext ctx) {
        ProviderAdapter p = providers.get(providerId);
        if (p == null) throw new IllegalArgumentException("Provider desconhecido: " + providerId);
        List<EmailEvent> out = new ArrayList<>(messageIds.size());
        for (String id : messageIds) out.add(p.getStatus(id, ctx));
        return out;
    }

    @Override
    public WebhookResult handleWebhook(String providerId, String body, String signatureHeader) {
        ProviderAdapter p = providers.get(providerId);
        if (p == null) return new WebhookResult(false, "unknown provider");
        return p.handleWebhook(body, signatureHeader, signatures::resolveSecret);
    }

    @Override
    public InboundResult handleInbound(String providerId, byte[] rawMime) {
        ProviderAdapter p = providers.get(providerId);
        if (p == null) return new InboundResult(false, "unknown provider");
        return p.handleInbound(rawMime);
    }

    @Override public Optional<String> resolveProvider(EmailContext ctx){ return Optional.ofNullable(router.route(ctx)); }
    @Override public Capabilities capabilities(String providerId){ return caps.get(providerId); }
}
