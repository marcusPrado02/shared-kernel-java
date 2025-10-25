package com.marcusprado02.sharedkernel.application.denorm.example.impl;

import java.util.HashMap;
import java.util.Map;

import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;

public final class OrderMappers {

    public static Map<String,Object> toFlatDoc(EventEnvelope e) {
        var m = new HashMap<String,Object>();
        m.put("id", e.metadata().aggregateId());
        m.put("tenant_id", e.metadata().tenantId());

        var p = payloadAsMap(e); // <- payload seguro como Map<String,Object>

        switch (String.valueOf(e.metadata().eventType())) {
            case "OrderCreated" -> {
                m.put("status", "CREATED");
                m.put("created_at", e.metadata().occurredAt());
                m.put("item_count", 0);
                m.put("total_value", java.math.BigDecimal.ZERO);
                m.put("deleted", false);
            }
            case "OrderItemAdded" -> {
                // No upsert incremental, espere que o produtor tenha colocado os agregados no payload
                // Ex.: { "item_count": 3, "total_value": 123.45 }
                m.putAll(p);
            }
            case "OrderStatusChanged" -> {
                m.put("status", String.valueOf(p.get("newStatus")));
                m.put("last_status_at", e.metadata().occurredAt());
            }
            case "OrderDeleted" -> {
                m.put("deleted", true);
                m.put("deleted_at", e.metadata().occurredAt());
            }
            default -> {
                // noop
            }
        }
        return m;
    }

    public static Map<String,Object> toSearchDoc(EventEnvelope e) {
        var m = new HashMap<String,Object>();
        m.put("id", e.metadata().aggregateId());
        m.put("tenantId", e.metadata().tenantId());

        var p = payloadAsMap(e);

        String status = switch (String.valueOf(e.metadata().eventType())) {
            case "OrderCreated"       -> "CREATED";
            case "OrderStatusChanged" -> String.valueOf(p.get("newStatus"));
            default                   -> null;
        };
        if (status != null) m.put("status", status);

        // occurredAt pode ser Instant/OffsetDateTime; ajuste conforme seu metadata
        // Se for java.time.Instant:
        try {
            m.put("ts", e.metadata().occurredAt().toEpochMilli());
        } catch (Throwable ignore) {
            // se não for Instant, apenas grave o objeto cru
            m.put("ts", e.metadata().occurredAt());
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    private static Map<String,Object> payloadAsMap(EventEnvelope e) {
        Object payload = e.payload();
        if (payload == null) return Map.of();

        // Caso típico: já é um Map
        if (payload instanceof Map<?,?> raw) {
            var out = new HashMap<String,Object>(raw.size());
            raw.forEach((k,v) -> out.put(String.valueOf(k), v));
            return out;
        }

        // Se seu payload for um tipo próprio (p.ex. record/POJO), aqui você pode:
        // - expor getters (via introspecção) ou
        // - delegar para um ObjectMapper compartilhado
        // Para manter sem dependência, devolvemos vazio e você especializa depois.
        return Map.of();
    }
}
