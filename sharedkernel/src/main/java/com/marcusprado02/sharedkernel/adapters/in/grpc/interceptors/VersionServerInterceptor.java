package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;

import com.marcusprado02.sharedkernel.adapters.in.rest.versioning.*;

import io.grpc.*;

public final class VersionServerInterceptor implements ServerInterceptor {
    private final VersionCatalog catalog;
    private final String logicalService; // ex.: "customer"
    public static final Metadata.Key<String> HDR = Metadata.Key.of("x-api-version", Metadata.ASCII_STRING_MARSHALLER);

    public VersionServerInterceptor(VersionCatalog catalog, String logicalService){
        this.catalog = catalog; this.logicalService = logicalService;
    }

    @Override public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                           ServerCallHandler<ReqT, RespT> next) {
        ApiVersion requested = headers.containsKey(HDR) ? ApiVersion.parse(headers.get(HDR)) : catalog.latest(logicalService);
        var served = catalog.best(logicalService, requested).orElseThrow();
        // guardar no Context
        var ctx = Context.current().withValue(Context.key("api.version"), served.toString());
        return Contexts.interceptCall(ctx, new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.put(HDR, served.toString());
                super.sendHeaders(responseHeaders);
            }
        }, headers, next);
    }
}