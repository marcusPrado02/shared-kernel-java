package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;


import io.grpc.*;
import java.util.Optional;

public class AuthServerInterceptor implements ServerInterceptor {
    private static final Metadata.Key<String> AUTH_HEADER =
      Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> CTX_SUBJECT = Context.key("subject");
    public static final Context.Key<String> CTX_TENANT = Context.key("tenant");

    @Override public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        String token = Optional.ofNullable(headers.get(AUTH_HEADER)).orElse("");
        // TODO: validar JWT (exp, iss, aud), extrair subject/tenant
        String subject = token.isBlank() ? "anonymous" : "user:123";
        String tenant  = "default-tenant";

        Context ctx = Context.current().withValues(CTX_SUBJECT, subject, CTX_TENANT, tenant);
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}

