package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;

import io.grpc.*;

public class IdempotencyServerInterceptor implements ServerInterceptor {
    public static final Metadata.Key<String> IDEM_KEY =
      Metadata.Key.of("idempotency-key", Metadata.ASCII_STRING_MARSHALLER);
    public static final Context.Key<String> CTX_IDEM_KEY = Context.key("idemKey");

    @Override public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
         ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String key = headers.get(IDEM_KEY);
        if (key != null && !key.isBlank()) {
            Context ctx = Context.current().withValue(CTX_IDEM_KEY, key);
            return Contexts.interceptCall(ctx, call, headers, next);
        }
        return next.startCall(call, headers);
    }
}