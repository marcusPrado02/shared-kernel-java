package com.marcusprado02.sharedkernel.adapters.in.grpc.example.services.impl;


import io.grpc.Metadata;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.time.ZoneOffset;
import java.util.Optional;

import com.example.users.v1.*;
import com.example.users.v1.UserServiceGrpc.UserServiceImplBase;
import com.marcusprado02.sharedkernel.contracts.protobuf.example.impl.UserAppPort;

import static com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors.IdempotencyServerInterceptor.CTX_IDEM_KEY;
import static com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors.CorrelationIdServerInterceptor.CTX_CORR_ID;

public class UserGrpcService extends UserServiceImplBase implements io.grpc.BindableService {

    private final UserAppPort app;

    public UserGrpcService(com.marcusprado02.sharedkernel.contracts.protobuf.example.impl.UserAppPort userApp) { this.app = userApp; }

    public void GetUser(GetUserRequest request, StreamObserver<User> responseObserver) {
        var dto = app.findById(request.getId()).orElse(null);
        if (dto == null) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
            return;
        }
        responseObserver.onNext(toProto(dto, ""));
        responseObserver.onCompleted();
    }

    public void ListUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        var spec = new UserAppPort.QuerySpec(
                request.getFiltersList().stream().map(f -> new UserAppPort.QuerySpec.Filter(f.getField(), f.getOp(), f.getValuesList())).toList(),
                request.getSortList().stream().map(s -> new UserAppPort.QuerySpec.SortBy(s.getField(), s.getDirection())).toList(),
                request.getPage().getCursor(), request.getPage().getSize(), request.getFields());
        var page = app.search(spec);
        var builder = ListUsersResponse.newBuilder()
                .setPage(PageResult.newBuilder()
                        .setNextCursor(page.page().nextCursor())
                        .setTotal(page.page().total())
                        .setHasMore(page.page().hasMore()));
        page.data().forEach(u -> builder.addData(toProto(u, request.getFields())));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    public void CreateUser(CreateUserRequest request, StreamObserver<User> responseObserver) {
        try {
            var idem = Optional.ofNullable(CTX_IDEM_KEY.get());
            var created = app.create(request.getName(), request.getEmail(), idem);
            responseObserver.onNext(toProto(created, ""));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Create failed").asRuntimeException());
        }
    }

    public void UpdateUser(UpdateUserRequest request, StreamObserver<User> responseObserver) {
        try {
            var updated = app.update(
                    request.getId(), request.getName(), request.getEmail(),
                    request.hasActive() ? Optional.of(request.getActive()) : Optional.empty(),
                    Optional.empty() // version not provided in request; use metadata If-Match if needed
            );
            responseObserver.onNext(toProto(updated, ""));
            responseObserver.onCompleted();
        } catch (IllegalStateException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    public void DeleteUser(DeleteUserRequest req, StreamObserver<com.google.protobuf.Empty> obs) {
        boolean ok = app.delete(req.getId());
        if (!ok) obs.onError(Status.NOT_FOUND.asRuntimeException());
        else { obs.onNext(com.google.protobuf.Empty.getDefaultInstance()); obs.onCompleted(); }
    }

    public StreamObserver<User> BulkUpsert(StreamObserver<UpsertResult> responseObserver) {
        String corr = Optional.ofNullable(CTX_CORR_ID.get()).orElse("n/a");
        return new StreamObserver<>() {
            int ok=0, fail=0;

            @Override public void onNext(User u) {
                try {
                    var dto = app.update(u.getId(), u.getName(), u.getEmail(),
                            Optional.of(u.getActive()), Optional.of(u.getVersion()));
                    responseObserver.onNext(UpsertResult.newBuilder().setId(dto.id()).setStatus("OK").build());
                    ok++;
                } catch (Exception e) {
                    responseObserver.onNext(UpsertResult.newBuilder().setId(u.getId()).setStatus("ERROR").setMessage(e.getMessage()).build());
                    fail++;
                }
            }
            @Override public void onError(Throwable t) { /* log com corr */ }
            @Override public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private User toProto(UserAppPort.UserDTO dto, String fieldsCsv) {
        var b = User.newBuilder()
           .setId(dto.id())
           .setName(dto.name())
           .setEmail(dto.email())
           .setActive(dto.active())
           .setCreatedAt(ts(dto.createdAt()))
           .setUpdatedAt(ts(dto.updatedAt()))
           .setVersion(dto.version());
        // opcional: aplicar sparse fields se quiser
        return b.build();
    }

    private static com.google.protobuf.Timestamp ts(java.time.OffsetDateTime t) {
        var i = t.toInstant();
        return com.google.protobuf.Timestamp.newBuilder()
          .setSeconds(i.getEpochSecond()).setNanos(i.getNano()).build();
    }

}

