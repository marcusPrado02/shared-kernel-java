package com.marcusprado02.sharedkernel.observability.tracing.decorators;


import java.util.Map;

import com.marcusprado02.sharedkernel.observability.tracing.*;

/** Hooks de ciclo de vida do span para padronizar atributos/eventos/status. */
public interface TracingSpanDecorator {
    /** Chamado antes de abrir o span (pode mutar o config retornando um novo). */
    default SpanConfig beforeStart(SpanConfig cfg){ return cfg; }

    /** Chamado logo após o span iniciar (ideal para setar atributos constantes). */
    default void afterStart(SpanHandle span, SpanConfig cfg) {}

    /** Chamado quando uma exceção for capturada e associada ao span. */
    default void onError(SpanHandle span, Throwable error) {}

    /** Chamado imediatamente antes do close(); pode ajustar status, métricas, etc. */
    default void beforeEnd(SpanHandle span) {}

    /** Chamado após close(); último ponto (log, métricas, etc.). */
    default void afterEnd(String traceId, String spanId, SpanConfig cfg) {}

    /** Helper para adicionar eventos semânticos. */
    default void onEvent(SpanHandle span, String name, Map<String,Object> attrs) {}
}

