package com.marcusprado02.sharedkernel.observability.tracing.decorators;


import java.util.Map;

import com.marcusprado02.sharedkernel.observability.tracing.*;

public final class HttpSemanticDecorator implements TracingSpanDecorator {
    @Override public void afterStart(SpanHandle span, SpanConfig cfg) {
        // Convenções aproximadas de OTel HTTP
        // cfg.attributes pode conter: http.method, http.route, http.target, net.peer.ip, user_agent
        // Ex.: setar defaults ausentes
        if (!cfg.attributes.containsKey("http.flavor")) span.setAttribute("http.flavor", "1.1");
    }
    @Override public void onError(SpanHandle span, Throwable error) {
        span.recordException(error, Map.of("error.type", error.getClass().getName()));
        span.setStatus(StatusCode.ERROR, error.getMessage()==null? "error" : error.getMessage());
    }
    @Override public void beforeEnd(SpanHandle span) {
        // Nada obrigatório; poderia avaliar métricas auxiliares ou flags
    }
}