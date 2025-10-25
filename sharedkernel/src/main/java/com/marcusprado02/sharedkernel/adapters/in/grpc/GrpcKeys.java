package com.marcusprado02.sharedkernel.adapters.in.grpc;


import io.grpc.Context;
import io.grpc.Metadata;

public final class GrpcKeys {
    private GrpcKeys(){}

    // Metadata keys (headers/trailers)
    public static final Metadata.Key<String> HDR_CORRELATION_ID =
            Metadata.Key.of("x-correlation-id", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> HDR_TENANT_ID =
            Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> HDR_IDEMPOTENCY_KEY =
            Metadata.Key.of("idempotency-key", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> HDR_AUTHZ =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    // io.grpc.Context keys (propagam no call/thread atual)
    public static final Context.Key<String> CTX_CORRELATION_ID = Context.key("correlationId");
    public static final Context.Key<String> CTX_TENANT_ID = Context.key("tenantId");
    public static final Context.Key<String> CTX_PRINCIPAL = Context.key("principal");
}
