package com.marcusprado02.sharedkernel.domain.exception.domain;

import com.marcusprado02.sharedkernel.domain.exception.model.ErrorCode;
import com.marcusprado02.sharedkernel.domain.exception.model.ErrorContext;
import com.marcusprado02.sharedkernel.domain.exception.model.Retryability;
import com.marcusprado02.sharedkernel.domain.exception.model.Severity;

public final class NotFoundException extends DomainException {
    public NotFoundException(String message, ErrorCode code, ErrorContext context) {
        super(message, null, code, Severity.LOW, Retryability.NONE, context, java.util.Map.of());
    }
}
