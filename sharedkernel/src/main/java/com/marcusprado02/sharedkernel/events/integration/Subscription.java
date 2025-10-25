package com.marcusprado02.sharedkernel.events.integration;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.cqrs.command.spi.MetricsFacade;
import com.marcusprado02.sharedkernel.cqrs.command.spi.TracerFacade;
import com.marcusprado02.sharedkernel.events.idempotency.ConsumerInbox;

public final class Subscription<E extends IntegrationEvent> {
    private final String topic; // ex.: "order"
    private final String group; // consumer group
    private final String eventType;
    private final IntegrationEventHandler<E> handler;
    private final ConsumerInbox inbox;
    private final TracerFacade tracer;
    private final MetricsFacade metrics;
    private final Upcaster upcaster;

    public Subscription(String topic, String group, String eventType, IntegrationEventHandler<E> handler,
                        ConsumerInbox inbox, TracerFacade tracer, MetricsFacade metrics, Upcaster upcaster) {
        this.topic=topic; this.group=group; this.eventType=eventType; this.handler=handler;
        this.inbox=inbox; this.tracer=tracer; this.metrics=metrics; this.upcaster=upcaster;
    }

    public CompletableFuture<Void> onMessage(String eventId, String payloadJson, Map<String,String> headers) throws Exception {
        var tags = TracerFacade.Tags.of("event.id", eventId, "type", eventType);
        TracerFacade.Span span = tracer.startSpan("event.consume", tags);
        try {
            if (inbox.seen(group, eventId)) {
                metrics.increment("event_duplicate", "type", eventType);
                return CompletableFuture.completedFuture(null);
            }
            var envelope = upcaster.upcast(eventType, payloadJson, headers);

            // extrai evento via reflexão: procura método "event()"
            @SuppressWarnings("unchecked")
            E ev = (E) tryExtractEvent(envelope);

            return handler.onEvent(envelope, ev)
                    .whenComplete((__, err) -> { if (err == null) inbox.record(group, eventId); })
                    .toCompletableFuture();

        } finally {
            span.end();
        }
    }

    private static Object tryExtractEvent(Object envelope) {
        if (envelope == null) return null;
        try {
            Method m = envelope.getClass().getMethod("event");
            return m.invoke(envelope);
        } catch (NoSuchMethodException no) {
            throw new IllegalStateException("Envelope does not expose event(): " + envelope.getClass().getName());
        } catch (ReflectiveOperationException re) {
            throw new RuntimeException("Failed to extract event() from envelope", re);
        }
    }

    public String topic(){ return topic; }
    public String group(){ return group; }
    public String eventType(){ return eventType; }
}
