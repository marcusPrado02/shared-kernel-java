package com.marcusprado02.sharedkernel.events.integration;


public interface IntegrationEventPublisher {
    void publish(IntegrationEvent event); // geralmente grava na Outbox
}
