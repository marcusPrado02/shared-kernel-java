package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;

import io.grpc.Metadata;
import io.grpc.StatusException;

public interface GrpcAuthorizer {
    /** Retorna principal (subject) se autorizado, caso contrário lança StatusException/Status.UNAUTHENTICATED/Status.PERMISSION_DENIED. */
    String authorize(String method, Metadata headers) throws StatusException;
}
