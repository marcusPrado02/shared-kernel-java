package com.marcusprado02.sharedkernel.domain.exception.domain;

import com.marcusprado02.sharedkernel.domain.exception.model.ErrorCode;
import com.marcusprado02.sharedkernel.domain.exception.model.ErrorContext;
import com.marcusprado02.sharedkernel.domain.exception.model.Retryability;
import com.marcusprado02.sharedkernel.domain.exception.model.Severity;

public final class PermissionDeniedException extends DomainException {
    public PermissionDeniedException(String message, ErrorCode code, ErrorContext context) {
        super(message, null, code, Severity.MEDIUM, Retryability.NONE, context, java.util.Map.of());
    }
}