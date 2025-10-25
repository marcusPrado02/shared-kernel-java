package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;


import com.google.rpc.ErrorInfo;
import com.google.rpc.Status;
import io.grpc.*;
import io.grpc.protobuf.StatusProto;

import java.util.Map;

public class ExceptionMappingServerInterceptor implements ServerInterceptor {
    @Override public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
          next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<>(call) {}, headers)) {
            @Override public void onHalfClose() {
                try { super.onHalfClose(); }
                catch (RuntimeException ex) {
                    var err = ErrorInfo.newBuilder()
                            .setReason(ex.getClass().getSimpleName())
                            .setDomain("userservice")
                            .putAllMetadata(Map.of("correlationId", String.valueOf(CorrelationIdServerInterceptor.CTX_CORR_ID.get())))
                            .build();
                    var status = Status.newBuilder()
                            .setCode(io.grpc.Status.INTERNAL.getCode().value())
                            .setMessage(ex.getMessage() == null ? "Internal error" : ex.getMessage())
                            .addDetails(com.google.protobuf.Any.pack(err))
                            .build();
                    call.close(StatusProto.toStatusRuntimeException(status).getStatus(), new Metadata());
                }
            }
        };
    }
}
