package com.marcusprado02.sharedkernel.adapters.example.impl;

import io.grpc.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.contracts.api.ApiExchange;
import com.marcusprado02.sharedkernel.contracts.api.ApiFilter;
import com.marcusprado02.sharedkernel.contracts.api.ApiRequest;
import com.marcusprado02.sharedkernel.contracts.api.EndpointHandler;
import com.marcusprado02.sharedkernel.contracts.api.FilterResult;

public final class GrpcApiFilterInterceptor implements ServerInterceptor {
    private final ApiFilter root;
    private final EndpointHandler terminal;

    public GrpcApiFilterInterceptor(ApiFilter root, EndpointHandler terminal){
        this.root = root; this.terminal = terminal;
    }

    @Override public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        var hdrs = headers.keys().stream().collect(java.util.stream.Collectors.toMap(
                k -> k, k -> String.valueOf(headers.get(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER)))
        ));
        var areq = new ApiRequest(call.getMethodDescriptor().getFullMethodName(), "/grpc/"+call.getMethodDescriptor().getFullMethodName(),
                Map.of(), hdrs, new byte[0], null, Instant.now(), java.util.Locale.ROOT);
        var ex = new ApiExchange(areq);
        try {
            root.apply(ex, _c -> {
                var out = terminal.handle(ex);
                ex.setResponse(out);
                return new FilterResult.Halt(ex);
            });
        } catch (Exception e) {
            call.close(Status.INTERNAL.withDescription(e.getMessage()), new Metadata());
            return new ServerCall.Listener<>() {};
        }
        return next.startCall(call, headers);
    }
}