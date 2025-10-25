package com.marcusprado02.sharedkernel.adapters.in.rest.versioning;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;

import java.io.IOException;

public final class VersionNegotiationFilter implements Filter {
    private final VersionNegotiator negotiator;
    private final String logicalType; // ex.: "customer" (por rota); pode vir de attribute

    public static final String ATTR_DECISION = "versionDecision";

    public VersionNegotiationFilter(VersionNegotiator negotiator, String logicalType){
        this.negotiator = negotiator; this.logicalType = logicalType;
    }

    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var req = (HttpServletRequest) request; var resp = (HttpServletResponse) response;
        var accept = req.getHeader("Accept");
        var acceptVersion = req.getHeader("Accept-Version");
        var xApiVersion = req.getHeader("X-Api-Version");

        var decision = negotiator.decide(logicalType, accept, acceptVersion, xApiVersion);

        // Expor headers e MDC
        resp.setHeader("Content-Type", decision.contentType);
        resp.setHeader("API-Version", decision.served.toString());
        resp.setHeader("Vary", String.join(", ", decision.varyOn));
        MDC.put("api.version.served", decision.served.toString());
        req.setAttribute(ATTR_DECISION, decision);

        chain.doFilter(request, response);
    }
}

