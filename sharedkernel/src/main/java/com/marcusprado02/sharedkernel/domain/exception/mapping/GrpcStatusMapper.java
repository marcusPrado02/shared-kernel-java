package com.marcusprado02.sharedkernel.domain.exception.mapping;

import com.marcusprado02.sharedkernel.domain.exception.domain.*;

import io.grpc.*;

public final class GrpcStatusMapper {

    public static StatusRuntimeException toStatus(DomainException ex) {
        Status status = switch (ex) {
            case ValidationException v      -> Status.INVALID_ARGUMENT;
            case PermissionDeniedException p-> Status.PERMISSION_DENIED;
            case NotFoundException n        -> Status.NOT_FOUND;
            case ConflictException c        -> Status.ALREADY_EXISTS;
            case ConcurrencyException c2    -> Status.ABORTED;
            case InvariantViolation inv     -> Status.FAILED_PRECONDITION;
            default                         -> Status.UNKNOWN;
        };
        status.withDescription(ex.getMessage());

        Metadata md = new Metadata();
        md.put(Metadata.Key.of("error-code", Metadata.ASCII_STRING_MARSHALLER), ex.codeFqn());
        md.put(Metadata.Key.of("retryability", Metadata.ASCII_STRING_MARSHALLER), ex.retryability().name());
        md.put(Metadata.Key.of("severity", Metadata.ASCII_STRING_MARSHALLER), ex.severity().name());
        if (ex.context() != null && ex.context().correlationId() != null) {
            md.put(Metadata.Key.of("correlation-id", Metadata.ASCII_STRING_MARSHALLER), ex.context().correlationId());
        }
        return status.asRuntimeException(md);
    }
}
