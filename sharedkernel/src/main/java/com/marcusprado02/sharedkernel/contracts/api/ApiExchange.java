package com.marcusprado02.sharedkernel.contracts.api;

import java.util.Objects;
import java.util.Optional;

/** Troca canônica (request + response + contexto). */
public final class ApiExchange {
    private final ApiRequest request;
    private ApiResponse response;
    private final ContextBag ctx = new ContextBag();

    public ApiExchange(ApiRequest request){ 
        this.request = Objects.requireNonNull(request); 
    }
    public ApiRequest request() { 
        return request; 
    }
    public Optional<ApiResponse> response() { 
        return Optional.ofNullable(response); 
    }
    public void setResponse(ApiResponse resp) { 
        this.response = resp; 
    }
    public ContextBag ctx() { 
        return ctx; 
    }
}