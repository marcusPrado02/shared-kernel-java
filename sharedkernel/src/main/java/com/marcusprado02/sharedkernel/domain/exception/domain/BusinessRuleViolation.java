package com.marcusprado02.sharedkernel.domain.exception.domain;

import com.marcusprado02.sharedkernel.domain.exception.model.ErrorCode;
import com.marcusprado02.sharedkernel.domain.exception.model.ErrorContext;
import com.marcusprado02.sharedkernel.domain.exception.model.Retryability;
import com.marcusprado02.sharedkernel.domain.exception.model.Severity;

public final class BusinessRuleViolation extends DomainException {
    public BusinessRuleViolation(String message, Throwable cause, ErrorCode code, Severity severity,
                                 Retryability retryability, ErrorContext context, java.util.Map<String,Object> params) {
        super(message, cause, code, severity, retryability, context, params);
    }
}
