package com.marcusprado02.sharedkernel.domain.exception.model;

import java.net.URI;
import java.util.Map;

public record ProblemDetails(
        URI type,
        String title,
        int status,
        String detail,
        String instance,
        String errorCode,                  // ErrorCode.fqn()
        Map<String, Object> context       // atributos extras
) {}