package com.marcusprado02.sharedkernel.adapters.in.grpc.example.client;

import com.example.users.v1.CreateUserRequest;
import com.example.users.v1.User;
import com.example.users.v1.UserServiceGrpc;
import com.example.users.v1.UserServiceGrpc.UserServiceBlockingStub;
import com.example.users.v1.UserServiceGrpc.UserServiceStub;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.UUID;

public class UserClient {
    private final UserServiceBlockingStub blocking;
    private final UserServiceStub         async;

    public UserClient(String host, int port, File trustCerts) throws SSLException {
        ManagedChannel channel = NettyChannelBuilder.forAddress(host, port)
                .useTransportSecurity()
                .sslContext(GrpcSslContexts.forClient().trustManager(trustCerts).build())
                .defaultServiceConfig(retryConfig())
                .enableRetry()
                .keepAliveTime(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        var md = new Metadata();
        md.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer <jwt>");
        md.put(Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER), UUID.randomUUID().toString());
        md.put(Metadata.Key.of("idempotency-key", Metadata.ASCII_STRING_MARSHALLER), UUID.randomUUID().toString());

        ClientInterceptor headers = MetadataUtils.newAttachHeadersInterceptor(md);
        Channel ch = ClientInterceptors.intercept(channel, headers);

        this.blocking = UserServiceGrpc.newBlockingStub(ch).withDeadlineAfter(3, java.util.concurrent.TimeUnit.SECONDS);
        this.async    = UserServiceGrpc.newStub(ch).withWaitForReady();
    }

    private static java.util.Map<String,Object> retryConfig() {
        return java.util.Map.of(
            "methodConfig", java.util.List.of(java.util.Map.of(
                "name", java.util.List.of(java.util.Map.of("service", "userservice.v1.UserService")),
                "retryPolicy", java.util.Map.of(
                   "maxAttempts", 4.0,
                   "initialBackoff", "0.2s",
                   "maxBackoff", "2s",
                   "backoffMultiplier", 2.0,
                   "retryableStatusCodes", java.util.List.of("UNAVAILABLE", "RESOURCE_EXHAUSTED")
                )
            ))
        );
    }

    public User create(String name, String email) {
        return blocking.createUser(CreateUserRequest.newBuilder()
                .setName(name)
                .setEmail(email)
                .build());
    }
}
