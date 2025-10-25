package com.marcusprado02.sharedkernel.adapters.in.rest.filters;

import com.marcusprado02.sharedkernel.contracts.api.ApiExchange;

public interface Authorizer {
    /** Deve lançar exceção ou retornar false quando não autorizado. */
    boolean isAuthorized(ApiExchange ex);
    String principal(ApiExchange ex);
}
