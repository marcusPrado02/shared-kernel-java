package com.marcusprado02.sharedkernel.crosscutting.interceptors.adapter.grpc;


import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Carrier;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.InterceptionContext;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;

import java.time.Instant;
import java.util.*;

public final class GrpcServerCtx implements InterceptionContext {
    private final ServerCall<?,?> call;
    private final Metadata headers;
    private final ServerCallHandler<?,?> next;
    private final Instant start = Instant.now();
    private final Map<String,Object> attrs = new HashMap<>();

    private GrpcServerCtx(ServerCall<?,?> call, Metadata headers, ServerCallHandler<?,?> next) {
        this.call = call; this.headers = headers; this.next = next;
    }

    public static GrpcServerCtx from(ServerCall<?,?> call, Metadata headers, ServerCallHandler<?,?> next) {
        return new GrpcServerCtx(call, headers, next);
    }

    public Object execute() {
        // startCall retorna o Listener do gRPC; devolvemos como Object para a chain
        @SuppressWarnings("unchecked")
        var listener = ((ServerCallHandler<Object,Object>) next).startCall((ServerCall<Object,Object>) call, headers);
        return listener;
    }

    @Override public String operation() {
        String method = call.getMethodDescriptor() != null ? call.getMethodDescriptor().getFullMethodName() : "unknown";
        return "gRPC " + method;
    }

    @Override public Map<String, Object> attributes() { return attrs; }

    @Override public Carrier carrier() {
        return new Carrier() {
            @Override public Optional<String> get(String key) {
                Metadata.Key<String> k = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
                return Optional.ofNullable(headers.get(k));
            }
            @Override public void set(String key, String value) {
                Metadata.Key<String> k = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
                headers.put(k, value);
            }
            @Override public Map<String, String> dump() {
                // Metadata não expõe iteração direta de pares; retornamos vazio para não custar caro
                return Collections.emptyMap();
            }
        };
    }

    @Override public Instant startTime() { return start; }
}