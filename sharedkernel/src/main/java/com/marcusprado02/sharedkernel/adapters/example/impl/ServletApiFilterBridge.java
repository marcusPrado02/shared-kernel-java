package com.marcusprado02.sharedkernel.adapters.example.impl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import com.marcusprado02.sharedkernel.contracts.api.ApiExchange;
import com.marcusprado02.sharedkernel.contracts.api.ApiFilter;
import com.marcusprado02.sharedkernel.contracts.api.ApiRequest;
import com.marcusprado02.sharedkernel.contracts.api.ApiResponse;
import com.marcusprado02.sharedkernel.contracts.api.EndpointHandler;
import com.marcusprado02.sharedkernel.contracts.api.FilterResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public final class ServletApiFilterBridge extends OncePerRequestFilter {
    private final ApiFilter root;            // cadeia construída
    private final EndpointHandler handler;   // handler terminal (resolve controller)

    public ServletApiFilterBridge(ApiFilter root, EndpointHandler handler){
        this.root = root; this.handler = handler;
    }

    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        try {
            var ex = toExchange(req);
            var chainWithHandler = (ApiFilter) (exchange, _c) -> root.apply(exchange, _x -> {
                // quando a cadeia chegar no terminal, chama o handler real
                var out = handler.handle(exchange);
                exchange.setResponse(out);
                return new FilterResult.Halt(exchange);
            });
            var result = chainWithHandler.apply(ex, null);
            var finalEx = (result instanceof FilterResult.Halt h) ? h.exchange() : ex;
            writeResponse(finalEx, resp);
        } catch (Exception e) {
            resp.setStatus(500);
            var msg = ("{\"error\":\"internal_error\",\"detail\":\""+ e.getClass().getSimpleName() +"\"}").getBytes(StandardCharsets.UTF_8);
            resp.getOutputStream().write(msg);
        }
    }

    private static ApiExchange toExchange(HttpServletRequest req) throws IOException {
        var headers = new LinkedHashMap<String, String>();
        var headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()){
            var name = headerNames.nextElement();
            headers.put(name.toLowerCase(), req.getHeader(name));
        }
        var query = new LinkedHashMap<String, List<String>>();
        req.getParameterMap().forEach((k,v) -> query.put(k, List.of(v)));

        var body = req.getInputStream().readAllBytes();
        var areq = new ApiRequest(req.getMethod(), req.getRequestURI(), query, headers, body,
                null, Instant.now(), req.getLocale());
        return new ApiExchange(areq);
    }

    private static void writeResponse(ApiExchange ex, HttpServletResponse resp) throws IOException {
        var out = ex.response().orElseGet(() -> ApiResponse.builder().status(204).finishedNow().build());
        resp.setStatus(out.status());
        out.headers().forEach((k, v) -> resp.setHeader((String) k, (String) v));
        if (out.body()!=null && out.body().length>0) resp.getOutputStream().write(out.body());
    }
}