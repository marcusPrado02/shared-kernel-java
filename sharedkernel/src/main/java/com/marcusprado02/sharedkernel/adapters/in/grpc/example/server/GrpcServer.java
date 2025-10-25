package com.marcusprado02.sharedkernel.adapters.in.grpc.example.server;


import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.ServerInterceptor;
import io.opentelemetry.api.OpenTelemetry;

import javax.net.ssl.SSLException;

import com.marcusprado02.sharedkernel.adapters.in.grpc.example.services.impl.UserGrpcService;
import com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors.*;
import com.marcusprado02.sharedkernel.contracts.protobuf.example.impl.UserAppPort;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class GrpcServer {

    private final int port;
    private final boolean enableReflection;
    private final boolean enableMtls;
    private final File certChain;
    private final File privateKey;
    private final File trustCerts;
    private final OpenTelemetry otel;
    private Server server;
    private final HealthStatusManager health = new HealthStatusManager();

    public GrpcServer(int port, boolean reflection, boolean mtls, File certChain, File privateKey, File trustCerts, OpenTelemetry otel) {
        this.port = port; this.enableReflection = reflection; this.enableMtls = mtls;
        this.certChain = certChain; this.privateKey = privateKey; this.trustCerts = trustCerts; this.otel = otel;
    }

    public void start(UserAppPort userApp) throws Exception {
        ServerInterceptor otelInterceptor = OtelServerInterceptorFactory.create(otel);

        var builder = NettyServerBuilder
                .forAddress(new InetSocketAddress("0.0.0.0", port))
                .maxInboundMessageSize(8 * 1024 * 1024)
                .permitKeepAliveTime(30, java.util.concurrent.TimeUnit.SECONDS)
                .permitKeepAliveWithoutCalls(true)
                .keepAliveTime(60, java.util.concurrent.TimeUnit.SECONDS)
                .addService(health.getHealthService())
                .addService(new UserGrpcService(userApp))
                .intercept(new CorrelationIdServerInterceptor())
                .intercept(new AuthServerInterceptor())
                .intercept(new RateLimitServerInterceptor(100, 50))
                .intercept(new IdempotencyServerInterceptor())
                .intercept(new ExceptionMappingServerInterceptor())
                .intercept(otelInterceptor);

        if (enableReflection) builder.addService(ProtoReflectionService.newInstance());
        if (enableMtls) builder.sslContext(GrpcSslContexts.forServer(certChain, privateKey).trustManager(trustCerts).clientAuth(io.netty.handler.ssl.ClientAuth.REQUIRE).build());

        server = builder.build().start();
        health.setStatus("userservice", io.grpc.health.v1.HealthCheckResponse.ServingStatus.SERVING);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        System.out.println("gRPC server started on :" + port);
        server.awaitTermination();
    }

    public void stop() {
        if (server != null) {
            health.clearStatus("userservice");
            server.shutdown();
        }
    }
}
