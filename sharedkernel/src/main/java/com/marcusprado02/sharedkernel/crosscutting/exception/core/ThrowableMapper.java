package com.marcusprado02.sharedkernel.crosscutting.exception.core;

public class ThrowableMapper<Ctx> implements ExceptionMapper<Throwable, Ctx> {
    @Override public MappedError map(Throwable ex, Ctx ctx) {
        return MappedError.builder()
            .status(ErrorCode.INTERNAL_ERROR.status).code(ErrorCode.INTERNAL_ERROR.code)
            .title(ErrorCode.INTERNAL_ERROR.title)
            .detail("Unexpected error") // nunca exponha stack ao cliente
            .build();
    }
}

