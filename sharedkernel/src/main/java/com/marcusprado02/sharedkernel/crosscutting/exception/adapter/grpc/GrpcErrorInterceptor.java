package com.marcusprado02.sharedkernel.crosscutting.exception.adapter.grpc;

import com.marcusprado02.sharedkernel.crosscutting.exception.core.ExceptionMapperRegistry;
import com.marcusprado02.sharedkernel.crosscutting.exception.core.MappedError;

public final class GrpcErrorInterceptor implements io.grpc.ServerInterceptor {

    private final ExceptionMapperRegistry<GrpcContext> registry;

    public record GrpcContext(io.grpc.ServerCall<?,?> call, io.grpc.Metadata headers) {}

    public GrpcErrorInterceptor(ExceptionMapperRegistry<GrpcContext> registry) { this.registry = registry; }

    @Override
    public <ReqT,RespT> io.grpc.ServerCall.Listener<ReqT> interceptCall(
        io.grpc.ServerCall<ReqT,RespT> call, io.grpc.Metadata headers, io.grpc.ServerCallHandler<ReqT,RespT> next) {

        var ctx = new GrpcContext(call, headers);
        var listener = next.startCall(call, headers);

        return new io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override public void onHalfClose() {
                try { super.onHalfClose(); }
                catch (Throwable ex) {
                    MappedError mapped = registry.map(ex, ctx);
                    io.grpc.Status status = mapToStatus(mapped.status(), mapped.errorCode(), mapped.title());
                    var md = new io.grpc.Metadata();
                    md.put(io.grpc.Metadata.Key.of("error-code", io.grpc.Metadata.ASCII_STRING_MARSHALLER), mapped.errorCode());
                    call.close(status.withDescription(mapped.detail()), md);
                }
            }
        };
    }

    private io.grpc.Status mapToStatus(int http, String code, String title) {
        io.grpc.Status base;
        switch (http) {
            case 400: base = io.grpc.Status.INVALID_ARGUMENT; break;
            case 401: base = io.grpc.Status.UNAUTHENTICATED; break;
            case 403: base = io.grpc.Status.PERMISSION_DENIED; break;
            case 404: base = io.grpc.Status.NOT_FOUND; break;
            case 409: base = io.grpc.Status.FAILED_PRECONDITION; break;
            case 429: base = io.grpc.Status.RESOURCE_EXHAUSTED; break;
            case 499: base = io.grpc.Status.CANCELLED; break;
            case 503: base = io.grpc.Status.UNAVAILABLE; break;
            default:  base = io.grpc.Status.UNKNOWN;
        }
        return base.withDescription(title + " [" + code + "]");
    }
}
