package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;


import com.marcusprado02.sharedkernel.adapters.in.grpc.BaseServerInterceptor;

import io.grpc.*;

public final class GuardrailsServerInterceptor extends BaseServerInterceptor {
    private final long minDeadlineMs;
    private final int maxInboundBytes;

    public GuardrailsServerInterceptor(long minDeadlineMs, int maxInboundBytes){
        this.minDeadlineMs = minDeadlineMs; this.maxInboundBytes = maxInboundBytes;
    }

    @Override
    protected <ReqT, RespT> ServerCall.Listener<ReqT> doIntercept(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        var deadline = Context.current().getDeadline();
        if (deadline == null || deadline.timeRemaining(java.util.concurrent.TimeUnit.MILLISECONDS) > minDeadlineMs) {
            // forçar um deadline upper bound para não travar server
            Context ctx = Context.current().withDeadlineAfter(minDeadlineMs, java.util.concurrent.TimeUnit.MILLISECONDS, 
                    java.util.concurrent.Executors.newSingleThreadScheduledExecutor());

            return Contexts.interceptCall(ctx, call, headers, next);
        }
        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override public void request(int numMessages) {
                super.request(numMessages);
            }
        }, headers);
    }

    public static void applyOnBuilder(io.grpc.netty.NettyServerBuilder b, int maxInboundBytes, String defaultCompression){
        b.maxInboundMessageSize(maxInboundBytes);
        if (defaultCompression != null) {
            b.compressorRegistry(io.grpc.CompressorRegistry.newEmptyInstance());
        }
    }
}
