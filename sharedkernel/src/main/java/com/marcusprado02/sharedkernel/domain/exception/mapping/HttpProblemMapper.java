package com.marcusprado02.sharedkernel.domain.exception.mapping;


import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.marcusprado02.sharedkernel.domain.exception.domain.*;
import com.marcusprado02.sharedkernel.domain.exception.model.ProblemDetails;

import java.net.URI;
import java.util.Map;

@RestControllerAdvice
public class HttpProblemMapper {

    private final MeterRegistry meter;

    public HttpProblemMapper(MeterRegistry meter) { this.meter = meter; }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetails> handle(DomainException ex) {
        HttpStatus status = switch (ex) {
            case ValidationException v      -> HttpStatus.BAD_REQUEST;
            case PermissionDeniedException p-> HttpStatus.FORBIDDEN;
            case NotFoundException n        -> HttpStatus.NOT_FOUND;
            case ConflictException c        -> HttpStatus.CONFLICT;
            case ConcurrencyException c2    -> HttpStatus.CONFLICT;
            case InvariantViolation inv     -> HttpStatus.UNPROCESSABLE_ENTITY;
            default                         -> HttpStatus.UNPROCESSABLE_ENTITY;
        };

        meter.counter("domain_exception_total",
                "code", ex.codeFqn(),
                "retryability", ex.retryability().name(),
                "severity", ex.severity().name(),
                "http_status", String.valueOf(status.value())).increment();

        var problem = new ProblemDetails(
                URI.create("urn:problem:"+ex.codeFqn()),
                ex.getClass().getSimpleName(),
                status.value(),
                ex.getMessage(),
                null, // instance path pode ser definido via filter
                ex.codeFqn(),
                Map.of(
                    "retryability", ex.retryability().name(),
                    "severity", ex.severity().name(),
                    "context", ex.context(),
                    "parameters", ex.parameters()
                )
        );
        return ResponseEntity.status(status).body(problem);
    }
}
