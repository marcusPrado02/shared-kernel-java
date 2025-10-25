package com.marcusprado02.sharedkernel.contracts.ws;

import java.time.OffsetDateTime;
import java.util.Map;

public record WsMessage<T>(
        String id,              // msg id (para ACK/idempotência se quiser)
        WsAction action,
        String channel,         // p.ex.: "prices:BTC-USD" | "orders:account:123"
        String op,              // semântica do publish/subscribe, ex.: "join", "setDepth"
        T payload,
        Map<String,Object> meta // client hints, filtros, cursors
) {}