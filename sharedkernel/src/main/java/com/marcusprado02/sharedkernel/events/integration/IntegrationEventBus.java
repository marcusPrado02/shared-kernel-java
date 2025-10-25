package com.marcusprado02.sharedkernel.events.integration;


import java.util.List;

public interface IntegrationEventBus {
    void subscribe(List<IntegrationEventHandler<? extends IntegrationEvent>> handlers);
}
