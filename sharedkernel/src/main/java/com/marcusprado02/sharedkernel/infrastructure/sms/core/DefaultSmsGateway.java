package com.marcusprado02.sharedkernel.infrastructure.sms.core;


import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.events.domain.DomainEvent;
import com.marcusprado02.sharedkernel.infrastructure.outbox.OutboxPublisher;
import com.marcusprado02.sharedkernel.infrastructure.sms.api.*;
import com.marcusprado02.sharedkernel.infrastructure.sms.model.*;
import com.marcusprado02.sharedkernel.infrastructure.sms.spi.*;
import com.marcusprado02.sharedkernel.infrastructure.sms.events.SmsSentEvent;

public class DefaultSmsGateway implements SmsGateway {

    private final Map<String, ProviderAdapter> providers = new HashMap<>();
    private final SmsRouter router;
    private final IdempotencyStore idem;
    private final OutboxPublisher outbox;
    private final SuppressionList suppression;
    private final SignatureRegistry signatures;
    private final TemplateRenderer renderer;
    private final Map<String, Capabilities> caps = new HashMap<>();

    private final Duration dedupeTtl = Duration.ofMinutes(10); // janela de dedupe

    public DefaultSmsGateway(Collection<ProviderFactory> factories,
                             ProviderConfigResolver cfgResolver,
                             SmsRouter router,
                             IdempotencyStore idem,
                             OutboxPublisher outbox,
                             SuppressionList suppression,
                             SignatureRegistry signatures,
                             TemplateRenderer renderer) {
        this.router = router; this.idem = idem; this.outbox = outbox;
        this.suppression = suppression; this.signatures = signatures; this.renderer = renderer;

        for (ProviderFactory f : factories) {
            ProviderConfig cfg = cfgResolver.resolve(f.providerId());
            ProviderAdapter adapter = f.create(cfg);
            providers.put(f.providerId(), adapter);
            caps.put(f.providerId(), adapter.metadata().capabilities());
        }
    }

    private ProviderAdapter pick(SmsContext ctx){
        String providerId = Optional.ofNullable(ctx.preferredProvider()).orElseGet(() -> router.route(ctx));
        ProviderAdapter adapter = providers.get(providerId);
        if (adapter == null) throw new IllegalArgumentException("Provider não encontrado: " + providerId);
        return adapter;
    }

    private SmsRequest renderIfNeeded(SmsRequest req) {
        if (req.body() != null && !req.body().isBlank()) return req;
        int maxLen = Optional.ofNullable(req.maxLength()).orElse(480);
        String body = renderer.render(req, maxLen);
        return new SmsRequest(req.idempotencyKey(), req.to(), body, req.templateId(), req.params(), req.locale(),
                req.context(), req.priority(), req.maxLength(), req.forcedEncoding(), req.metadata(), req.requireDeliveryReport());
    }

    private void enforceCompliance(SmsRequest req){
        if (suppression.isSuppressed(req.context().tenantId(), req.to().e164()))
            throw new IllegalStateException("Número opt-out/suppressed");
    }

    @Override public SmsResponse send(SmsRequest rawReq, Policy policy) {
        enforceCompliance(rawReq);
        SmsRequest req = renderIfNeeded(rawReq);
        return idem.find(req.idempotencyKey()).orElseGet(() -> {
            SmsResponse res = pick(req.context()).send(req, policy);
            idem.save(req.idempotencyKey(), res, dedupeTtl);
            if (policy.outboxEnabled()) {
                SmsSentEvent event = new SmsSentEvent(res.messageId(), rawReq.to().toString(), res.status().name(),
                        res.error().name(), rawReq.context().tenantId(), rawReq.context());
                outbox.publish(List.of(event));
            }
            return res;
        });
    }

    @Override public List<SmsResponse> sendBulk(BulkRequest bulk, Policy policy) {
        List<SmsRequest> normalized = new ArrayList<>();
        for (SmsRequest r : bulk.messages()) { enforceCompliance(r); normalized.add(renderIfNeeded(r)); }
        List<SmsResponse> res = pick(normalized.get(0).context()).sendBulk(new BulkRequest(normalized), policy);
        if (policy.outboxEnabled()) {
            res.forEach(r -> {
                SmsRequest rawReq = bulk.messages().stream().filter(m -> m.idempotencyKey().equals(r.idempotencyKey())).findFirst().orElse(null);
                if (rawReq != null) {
                    SmsSentEvent event = new SmsSentEvent(r.messageId(), rawReq.to().toString(), r.status().name(),
                            r.error().name(), rawReq.context().tenantId(), rawReq.context());
                    outbox.publish(List.of(event));
                }
            });
        }
        return res;
    }

    @Override public CompletableFuture<SmsResponse> sendAsync(SmsRequest req, Policy p) { return pick(req.context()).sendAsync(renderIfNeeded(req), p); }
    @Override public CompletableFuture<List<SmsResponse>> sendBulkAsync(BulkRequest req, Policy p) { return CompletableFuture.supplyAsync(() -> sendBulk(req, p)); }

    @Override public DeliveryReport getDeliveryReport(String providerId, String messageId, SmsContext ctx) {
        ProviderAdapter adapter = providers.get(providerId);
        if (adapter == null) throw new IllegalArgumentException("Provider desconhecido");
        return adapter.getDeliveryReport(messageId, ctx);
    }

    @Override public WebhookResult handleWebhook(String providerId, String body, String signatureHeader) {
        ProviderAdapter adapter = providers.get(providerId);
        if (adapter == null) return new WebhookResult(false, "unknown provider");
        return adapter.handleWebhook(body, signatureHeader, signatures::resolveSecret);
    }

    @Override public Optional<String> resolveProvider(SmsContext ctx) { return Optional.ofNullable(router.route(ctx)); }
    @Override public Capabilities capabilities(String providerId) { return caps.get(providerId); }

    @Override
    public List<DeliveryReport> getDeliveryReportsBulk(String providerId, List<String> messageIds, SmsContext ctx) {
        ProviderAdapter adapter = providers.get(providerId);
        if (adapter == null) throw new IllegalArgumentException("Provider desconhecido: " + providerId);
        List<DeliveryReport> out = new ArrayList<>(messageIds.size());
        for (String mid : messageIds) {
            out.add(adapter.getDeliveryReport(mid, ctx));
        }
        return out;
    }
}
