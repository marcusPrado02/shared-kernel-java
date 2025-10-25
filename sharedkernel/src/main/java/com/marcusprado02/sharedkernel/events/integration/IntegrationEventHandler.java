package com.marcusprado02.sharedkernel.events.integration;

import java.util.concurrent.CompletionStage;

public interface IntegrationEventHandler<E extends IntegrationEvent> {
    String eventType();  // "order.created.v1"
    CompletionStage<Void> onEvent(IntegrationEventEnvelope envelope, E event) throws Exception;
}
