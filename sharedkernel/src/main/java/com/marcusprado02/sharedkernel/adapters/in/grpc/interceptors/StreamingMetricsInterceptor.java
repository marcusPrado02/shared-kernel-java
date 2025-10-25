package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;

import com.marcusprado02.sharedkernel.adapters.in.grpc.BaseServerInterceptor;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;

public final class StreamingMetricsInterceptor extends BaseServerInterceptor {
    @Override protected <ReqT, RespT> ServerCall.Listener<ReqT> doIntercept(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        final String method = call.getMethodDescriptor().getFullMethodName();
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(next.startCall(call, headers)) {
            int in = 0;
            @Override public void onMessage(ReqT message) {
                in++;
                super.onMessage(message);
            }
            @Override public void onHalfClose() {
                // finalize input; útil para logs/metricas
                super.onHalfClose();
            }
            @Override public void onCancel() {
                // cliente cancelou; registrar métricas
                super.onCancel();
            }
            @Override public void onComplete() {
                // emitir métrica ex.: grpc_stream_in_messages_total{method} = in
                super.onComplete();
            }
        };
    }
}
