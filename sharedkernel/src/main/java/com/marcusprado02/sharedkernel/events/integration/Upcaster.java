package com.marcusprado02.sharedkernel.events.integration;

import java.util.Map;

public interface Upcaster {
    /** Converte (type+json antigos) para o envelope e tipo atual. */
    IntegrationEventEnvelope upcast(String eventType, String payloadJson, Map<String,String> headers);

   // ⬇️ sem JsonEvents.parse; delega para um factory da IntegrationEventEnvelope
    static Upcaster noop(){
        return (type, json, hdrs) -> IntegrationEventEnvelope.fromLegacy(type, json, hdrs);
    }
}
