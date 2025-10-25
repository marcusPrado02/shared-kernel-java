package com.marcusprado02.sharedkernel.crosscutting.middleware.impl;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.marcusprado02.sharedkernel.crosscutting.middleware.core.Middleware;
import com.marcusprado02.sharedkernel.crosscutting.middleware.core.MiddlewareChain;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@SuppressWarnings("rawtypes")
public class TracingMiddleware implements Middleware<HttpRequest, HttpResponse> {
    private final Tracer tracer;

    public TracingMiddleware(Tracer tracer) { this.tracer = tracer; }

    @Override
    public HttpResponse invoke(HttpRequest request, MiddlewareChain<HttpRequest, HttpResponse> chain) throws Exception {
        Span span = tracer.spanBuilder("http.request").startSpan();
        try (Scope scope = span.makeCurrent()) {
            if (request != null) {
                span.setAttribute("http.request.method", request.method());
                span.setAttribute("http.request.path", request.uri().getPath());
            }
            HttpResponse response = chain.next(request);
            if (response != null) {
                span.setAttribute("http.response.status_code", response.statusCode());
            }
            return response;
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
