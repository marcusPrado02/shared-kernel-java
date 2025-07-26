package com.marcusprado02.sharedkernel.domain.exception;

import java.util.Map;

/**
 * Lançada quando algo dá errado na geração de ID.
 */
public class IdGenerationException extends DomainException {

    private static final String CODE = "ID_GENERATION_FAILED";

    public IdGenerationException(String detail) {
        super(CODE, "Fail to generate ID: " + detail, Map.of("detail", detail));
    }
}

