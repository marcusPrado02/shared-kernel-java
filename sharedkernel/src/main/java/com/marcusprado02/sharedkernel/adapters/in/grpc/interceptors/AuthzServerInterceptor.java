package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;

import com.marcusprado02.sharedkernel.adapters.in.grpc.BaseServerInterceptor;
import com.marcusprado02.sharedkernel.adapters.in.grpc.GrpcKeys;

import io.grpc.*;
import io.grpc.Metadata;

public final class AuthzServerInterceptor extends BaseServerInterceptor {
    private final GrpcAuthorizer authz;
    public AuthzServerInterceptor(GrpcAuthorizer a){ this.authz = a; }

    @Override
    protected <ReqT, RespT> ServerCall.Listener<ReqT> doIntercept(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String principal;
        try {
            principal = authz.authorize(call.getMethodDescriptor().getFullMethodName(), headers);
        } catch (io.grpc.StatusException e) {
            call.close(e.getStatus(), headers);
            return new ServerCall.Listener<ReqT>() {};
        }
        Context ctx = Context.current().withValue(GrpcKeys.CTX_PRINCIPAL, principal);
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
