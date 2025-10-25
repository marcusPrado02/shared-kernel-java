package com.marcusprado02.sharedkernel.crosscutting.context;


import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptionContext;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Carrier; // ajuste o pacote se necessário

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class HttpServerCtx implements InterceptionContext {
    private final HttpServletRequest req;
    private final HttpServletResponse res;
    private final Object handler;
    private final Instant start = Instant.now();
    private final Map<String,Object> attrs = new HashMap<>();

    private HttpServerCtx(HttpServletRequest req, HttpServletResponse res, Object handler) {
        this.req = req; this.res = res; this.handler = handler;
    }

    public static HttpServerCtx from(HttpServletRequest req, HttpServletResponse res, Object handler) {
        return new HttpServerCtx(req, res, handler);
    }

    // --- API específica útil
    public HttpServletRequest request() { return req; }
    public HttpServletResponse response() { return res; }
    public Object handler() { return handler; }

    // --- InterceptionContext
    @Override public String operation() {
        String path = (req != null ? req.getRequestURI() : "");
        String method = (req != null ? req.getMethod() : "UNKNOWN");
        return method + " " + path;
    }

    @Override public Map<String, Object> attributes() { return attrs; }

    @Override public Carrier carrier() {
        return new Carrier() {
            @Override public Optional<String> get(String key) {
                if (req == null) return Optional.empty();
                String v = req.getHeader(key);
                return Optional.ofNullable(v);
            }
            @Override public void set(String key, String value) {
                if (res != null) res.setHeader(key, value);
            }
            @Override public Map<String, String> dump() {
                if (req == null) return Collections.emptyMap();
                var map = new java.util.LinkedHashMap<String,String>();
                var names = req.getHeaderNames();
                while (names.hasMoreElements()) {
                    String n = names.nextElement();
                    map.put(n, req.getHeader(n));
                }
                return map;
            }
        };
    }

    @Override public Instant startTime() { return start; }

    // --- hooks usados no adapter anterior (mantidos por compatibilidade)
    public Object invokeHandler() { return null; }           // no-op
    public boolean shouldContinue(Object result) { return true; }
}