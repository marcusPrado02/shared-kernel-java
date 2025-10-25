package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;

import io.grpc.*;

import java.util.UUID;

public class CorrelationIdServerInterceptor implements ServerInterceptor {
    public static final Metadata.Key<String> CORR_ID =
            Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> CTX_CORR_ID = Context.key("corrId");

    @Override public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String idFromHeader = headers.get(CORR_ID);
        final String corrId = (idFromHeader == null || idFromHeader.isBlank()) ? UUID.randomUUID().toString() : idFromHeader;
        Context ctx = Context.current().withValue(CTX_CORR_ID, corrId);

        return Contexts.interceptCall(ctx, new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.put(CORR_ID, corrId);
                super.sendHeaders(responseHeaders);
            }
        }, headers, next);
    }
}
