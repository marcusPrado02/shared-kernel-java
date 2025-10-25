package com.marcusprado02.sharedkernel.contracts.ws;

import java.time.OffsetDateTime;

public record WsEvent<T>(
        String channel,
        String eventType,       // p.ex.: "price.tick", "order.updated"
        T data,
        OffsetDateTime occurredAt,
        String version
) {}
