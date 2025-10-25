package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;


import io.grpc.ServerInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;

public class OtelServerInterceptorFactory {
    public static ServerInterceptor create(OpenTelemetry otel) {
        return GrpcTelemetry.create(otel).newServerInterceptor();
    }
}