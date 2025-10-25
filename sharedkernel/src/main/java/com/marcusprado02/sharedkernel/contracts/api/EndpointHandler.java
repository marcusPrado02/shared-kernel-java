package com.marcusprado02.sharedkernel.contracts.api;

/** Contrato do handler final a ser decorado pela cadeia. */
@FunctionalInterface
public interface EndpointHandler {
    ApiResponse handle(ApiExchange exchange) throws Exception;
}
