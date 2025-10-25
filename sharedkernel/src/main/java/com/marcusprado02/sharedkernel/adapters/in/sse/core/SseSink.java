package com.marcusprado02.sharedkernel.adapters.in.sse.core;

import java.time.Instant;
import java.util.Map;

public interface SseSink {
    /** Envia para um canal lógico (topic). Retorna false se droppado por backpressure. */
    boolean emit(String topic, SseMessage msg);
    /** Fecha todas as assinaturas de um tópico. */
    void closeTopic(String topic, String reason);
}
