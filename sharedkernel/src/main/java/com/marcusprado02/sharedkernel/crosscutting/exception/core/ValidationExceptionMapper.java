package com.marcusprado02.sharedkernel.crosscutting.exception.core;

import java.util.List;

public class ValidationExceptionMapper<Ctx> implements ExceptionMapper<Exception, Ctx> {
    @Override public MappedError map(Exception ex, Ctx ctx) {
        List<FieldErrorDTO> fields = extract(ex);
        return MappedError.builder()
            .status(ErrorCode.VALIDATION_ERROR.status).code(ErrorCode.VALIDATION_ERROR.code)
            .title(ErrorCode.VALIDATION_ERROR.title)
            .detail("One or more fields are invalid.")
            .extra("fieldErrors", fields).build();
    }
    private List<FieldErrorDTO> extract(Exception ex){
        // Implementar: para Spring, iterar BindingResult; para Jakarta, ConstraintViolation
        return List.of();
    }
}
