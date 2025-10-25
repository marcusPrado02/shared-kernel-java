package com.marcusprado02.sharedkernel.crosscutting.exception.core;

public class SecurityExceptionMapper<Ctx> implements ExceptionMapper<SecurityException, Ctx> {
    @Override public MappedError map(SecurityException ex, Ctx ctx) {
        return MappedError.builder()
            .status(ErrorCode.FORBIDDEN.status).code(ErrorCode.FORBIDDEN.code)
            .title(ErrorCode.FORBIDDEN.title)
            .detail("You are not allowed to perform this operation.")
            .build();
    }
}

