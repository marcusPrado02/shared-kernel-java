package com.marcusprado02.sharedkernel.adapters.in.grpc;

import io.grpc.*;

public abstract class BaseServerInterceptor implements ServerInterceptor {
    @Override
    public final <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        try {
            return doIntercept(call, headers, next);

        } catch (StatusRuntimeException e) {
            // Extrai Status e trailers de forma segura
            Status status = Status.fromThrowable(e);
            Metadata trailers = (e.getTrailers() != null)
                    ? e.getTrailers()
                    : new Metadata();

            call.close(status, trailers);
            return new ServerCall.Listener<>() {};

        } catch (Throwable t) {
            call.close(Status.INTERNAL.withDescription(t.getClass().getSimpleName()).withCause(t), new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }

    protected abstract <ReqT, RespT> ServerCall.Listener<ReqT> doIntercept(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next);
}
