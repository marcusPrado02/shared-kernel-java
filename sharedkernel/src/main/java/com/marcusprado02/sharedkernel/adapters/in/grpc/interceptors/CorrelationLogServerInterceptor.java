package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marcusprado02.sharedkernel.adapters.in.grpc.BaseServerInterceptor;
import com.marcusprado02.sharedkernel.adapters.in.grpc.GrpcKeys;

import java.util.UUID;

public final class CorrelationLogServerInterceptor extends BaseServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CorrelationLogServerInterceptor.class);

    @Override
    protected <ReqT, RespT> ServerCall.Listener<ReqT> doIntercept(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        final String cid = resolveCid(headers); // <- final/efetivamente final
        final String method = call.getMethodDescriptor().getFullMethodName();
        final String tenant = headers.get(GrpcKeys.HDR_TENANT_ID);

        ServerCall<ReqT, RespT> forwarding = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            long t0 = System.nanoTime();
            @Override public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.put(GrpcKeys.HDR_CORRELATION_ID, cid);
                super.sendHeaders(responseHeaders);
            }
            @Override public void close(Status status, Metadata trailers) {
                long ms = (System.nanoTime() - t0) / 1_000_000;
                log.info("gRPC {} cid={} tenant={} status={} dtMs={}",
                        method, cid, redact(tenant), status.getCode().name(), ms);
                super.close(status, trailers);
            }
        };

        Context ctx = Context.current()
                .withValue(GrpcKeys.CTX_CORRELATION_ID, cid)
                .withValue(GrpcKeys.CTX_TENANT_ID, tenant);

        return Contexts.interceptCall(ctx, forwarding, headers, next);
    }

    private static String resolveCid(Metadata headers) {
        String h = headers.get(GrpcKeys.HDR_CORRELATION_ID);
        return (h == null || h.isBlank()) ? UUID.randomUUID().toString() : h;
    }

    private String redact(String s) {
        return (s == null || s.isBlank()) ? "-" : (s.length() <= 2 ? "***" : s.substring(0, 2) + "***");
    }
}
