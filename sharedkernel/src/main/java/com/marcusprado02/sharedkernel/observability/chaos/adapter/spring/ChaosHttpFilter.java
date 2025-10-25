package com.marcusprado02.sharedkernel.observability.chaos.adapter.spring;

import org.slf4j.MDC;

import com.marcusprado02.sharedkernel.observability.chaos.ChaosContext;
import com.marcusprado02.sharedkernel.observability.chaos.ChaosEngine;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

public final class ChaosHttpFilter implements Filter {
    private final ChaosEngine engine;
    private final String headerToken;

    public ChaosHttpFilter(ChaosEngine engine, String headerToken){ this.engine=engine; this.headerToken=headerToken; }

    @Override public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) req;

        var ctx = new ChaosContext(
                http.getRequestURI(),
                http.getMethod(),
                MDC.get("tenant"),
                MDC.get("user"),
                MDC.get("trace_id"),
                Map.of("chaos.token", http.getHeader(headerToken==null? "X-Chaos-Key" : headerToken))
        );

        try {
            engine.maybeInject(ctx);
            chain.doFilter(req, res);
        } catch (Exception chaosEx) {
            // Deixe a exceção estourar para a app (é uma falha intencional)
            throw new ServletException(chaosEx);
        }
    }
}
