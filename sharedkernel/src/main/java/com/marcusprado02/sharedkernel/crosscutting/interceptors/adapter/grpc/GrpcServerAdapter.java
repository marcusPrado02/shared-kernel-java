package com.marcusprado02.sharedkernel.crosscutting.interceptors.adapter.grpc;

import java.util.List;

import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.DefaultInterceptorChain;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Interceptor;

public class GrpcServerAdapter implements io.grpc.ServerInterceptor {
    private final List<Interceptor<GrpcServerCtx>> interceptors;
    public GrpcServerAdapter(List<Interceptor<GrpcServerCtx>> interceptors) { this.interceptors = interceptors; }

    @Override
    public <ReqT, RespT> io.grpc.ServerCall.Listener<ReqT> interceptCall(
        io.grpc.ServerCall<ReqT, RespT> call, io.grpc.Metadata headers, io.grpc.ServerCallHandler<ReqT, RespT> next) {

        var ctx = GrpcServerCtx.from(call, headers, next);
        var chain = new DefaultInterceptorChain<>(interceptors, c -> c.execute());
        try {
            @SuppressWarnings("unchecked")
            io.grpc.ServerCall.Listener<ReqT> listener = (io.grpc.ServerCall.Listener<ReqT>) chain.proceed(ctx);
            return listener;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
