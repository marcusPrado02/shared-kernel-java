package com.marcusprado02.sharedkernel.domain.exception.domain;


import java.util.Map;

import com.marcusprado02.sharedkernel.domain.exception.model.ErrorCode;
import com.marcusprado02.sharedkernel.domain.exception.model.ErrorContext;
import com.marcusprado02.sharedkernel.domain.exception.model.Retryability;
import com.marcusprado02.sharedkernel.domain.exception.model.Severity;

public sealed class DomainException extends RuntimeException
        permits BusinessRuleViolation, InvariantViolation, NotFoundException,
                ConflictException, ValidationException, PermissionDeniedException,
                ConcurrencyException {

    private final ErrorCode code;
    private final Severity severity;
    private final Retryability retryability;
    private final ErrorContext context;
    private final Map<String, Object> parameters; // p/ i18n templates

    protected DomainException(
            String message,
            Throwable cause,
            ErrorCode code,
            Severity severity,
            Retryability retryability,
            ErrorContext context,
            Map<String, Object> parameters) {
        super(message, cause);
        this.code = code;
        this.severity = severity;
        this.retryability = retryability;
        this.context = context;
        this.parameters = Map.copyOf(parameters == null ? Map.of() : parameters);
    }

    public ErrorCode code() { return code; }
    public Severity severity() { return severity; }
    public Retryability retryability() { return retryability; }
    public ErrorContext context() { return context; }
    public Map<String, Object> parameters() { return parameters; }

    public String codeFqn(){ return code.fqn(); }

    /** Construtor estático para builder-like ergonomics. */
    public static Builder builder(ErrorCode code) { return new Builder(code); }

    public boolean clientSafe() {
        return context().isClientSafe();
    }

    /** Compat para código legado que espera args(); mapeia para parameters(). */
    public Map<String, Object> args() {
        return parameters();
    }

    public static final class Builder {
        private final ErrorCode code;
        private String message;
        private Throwable cause;
        private Severity severity = Severity.MEDIUM;
        private Retryability retryability = Retryability.NONE;
        private ErrorContext context = ErrorContext.minimal(null);
        private Map<String, Object> params = Map.of();

        public Builder(ErrorCode code){ this.code = code; }
        public Builder message(String m){ this.message = m; return this; }
        public Builder cause(Throwable c){ this.cause = c; return this; }
        public Builder severity(Severity s){ this.severity = s; return this; }
        public Builder retryability(Retryability r){ this.retryability = r; return this; }
        public Builder context(ErrorContext c){ this.context = c; return this; }
        public Builder params(Map<String,Object> p){ this.params = p; return this; }

        /** Por padrão cria BusinessRuleViolation; subclasses podem expor factories próprias. */
        public BusinessRuleViolation buildBusiness() {
            return new BusinessRuleViolation(message, cause, code, severity, retryability, context, params);
        }
    }
}
