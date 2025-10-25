package com.marcusprado02.sharedkernel.crosscutting.exception.adapter.rest;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.marcusprado02.sharedkernel.crosscutting.exception.core.ExceptionMapperRegistry;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ExceptionMapperRegistry<HttpServletRequest> registry;
    private final io.opentelemetry.api.trace.Tracer tracer;

    public GlobalExceptionHandler(ExceptionMapperRegistry<HttpServletRequest> registry,
                                  io.opentelemetry.api.trace.Tracer tracer) {
        this.registry = registry; this.tracer = tracer;
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Map<String,Object>> handle(Throwable ex, HttpServletRequest req) {
        var mapped = registry.map(ex, req);
        var traceId = io.opentelemetry.api.trace.Span.current().getSpanContext().getTraceId();
        var body = ProblemJson.of(mapped, "https://errors.acme.com", traceId);

        var headers = new HttpHeaders();
        mapped.headers().forEach(headers::add);
        headers.add(HttpHeaders.CONTENT_TYPE, "application/problem+json");

        // Telemetria mínima
        io.opentelemetry.api.trace.Span.current().setAttribute("error.code", mapped.errorCode());
        io.opentelemetry.api.trace.Span.current().recordException(ex);

        return new ResponseEntity<>(body, headers, HttpStatus.valueOf(mapped.status()));
    }
}
