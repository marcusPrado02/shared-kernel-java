package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;

import com.marcusprado02.sharedkernel.adapters.in.grpc.BaseServerInterceptor;
import com.marcusprado02.sharedkernel.adapters.in.grpc.GrpcKeys;

import io.grpc.*;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

public final class OtelServerInterceptor extends BaseServerInterceptor {
    @Override
    protected <ReqT, RespT> ServerCall.Listener<ReqT> doIntercept(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        var tracer = GlobalOpenTelemetry.getTracer("forgestack.grpc");
        var span = tracer.spanBuilder(call.getMethodDescriptor().getFullMethodName())
                .setSpanKind(io.opentelemetry.api.trace.SpanKind.SERVER).startSpan();

        span.setAttribute("rpc.system", "grpc");
        span.setAttribute("rpc.method", call.getMethodDescriptor().getFullMethodName());
        var cid = headers.get(GrpcKeys.HDR_CORRELATION_ID);
        if (cid != null) span.setAttribute("correlation.id", cid);

        Scope scope = span.makeCurrent();
        try {
            return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(next.startCall(
                    new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
                        @Override public void close(Status status, Metadata trailers) {
                            if (!status.isOk()) {
                                span.recordException(status.asRuntimeException());
                                span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR);
                            }
                            super.close(status, trailers);
                            span.end();
                            scope.close();
                        }
                    }, headers)) {};
        } catch (Throwable t){
            span.recordException(t); span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR); span.end(); scope.close(); throw t;
        }
    }
}