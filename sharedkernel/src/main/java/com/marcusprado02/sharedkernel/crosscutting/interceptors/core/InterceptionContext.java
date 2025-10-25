package com.marcusprado02.sharedkernel.crosscutting.interceptors.core;

import java.time.Instant;
import java.util.Map;

public interface InterceptionContext {
    String operation();             // ex.: HTTP GET /orders, gRPC method, topic:partition
    Map<String, Object> attributes(); // tenantId, correlationId, auth, baggage
    Carrier carrier();              // leitura/escrita de headers/metadata
    Instant startTime();
}
