package com.marcusprado02.sharedkernel.adapters.in.grpc;

import io.grpc.*;

public final class VersionClientInterceptor implements ClientInterceptor {
    private final String version; // "2" ou "2.1"
    private static final Metadata.Key<String> HDR = Metadata.Key.of("x-api-version", Metadata.ASCII_STRING_MARSHALLER);
    public VersionClientInterceptor(String v){ this.version = v; }
    @Override public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(HDR, version);
                super.start(responseListener, headers);
            }
        };
    }
}

