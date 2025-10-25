package com.marcusprado02.sharedkernel.domain.exception.domain;

import com.marcusprado02.sharedkernel.domain.exception.model.ErrorCode;
import com.marcusprado02.sharedkernel.domain.exception.model.ErrorContext;
import com.marcusprado02.sharedkernel.domain.exception.model.Retryability;
import com.marcusprado02.sharedkernel.domain.exception.model.Severity;

import java.util.Map;

public final class ValidationException extends DomainException {
    public ValidationException(String message, ErrorCode code, ErrorContext context, Map<String,Object> params) {
        super(message, null, code, Severity.LOW, Retryability.NONE, context, params);
    }
}