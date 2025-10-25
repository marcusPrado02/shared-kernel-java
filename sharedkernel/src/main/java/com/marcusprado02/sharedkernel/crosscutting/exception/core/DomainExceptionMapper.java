package com.marcusprado02.sharedkernel.crosscutting.exception.core;

import java.util.Map;

import com.marcusprado02.sharedkernel.domain.exception.domain.DomainException;

public class DomainExceptionMapper<Ctx> implements ExceptionMapper<DomainException, Ctx> {
    @Override public MappedError map(DomainException ex, Ctx ctx) {
        return MappedError.builder()
            .status(409)
            .code(String.valueOf(ex.code()))
            .title("Domain rule violation")
            .detail(ex.clientSafe() ? ex.getMessage() : "Business rule violated")
            .extra("args", safeArgs(ex.args()))
            .build();
    }
    private Map<String,Object> safeArgs(Map<String,Object> a){
        // TODO: aplicar redator/masking se necessário
        return a == null ? Map.of() : a;
    }
}
