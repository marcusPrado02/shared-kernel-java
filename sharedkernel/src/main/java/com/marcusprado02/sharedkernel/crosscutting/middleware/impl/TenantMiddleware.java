package com.marcusprado02.sharedkernel.crosscutting.middleware.impl;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.marcusprado02.sharedkernel.crosscutting.context.TenantContext;
import com.marcusprado02.sharedkernel.crosscutting.middleware.core.Middleware;
import com.marcusprado02.sharedkernel.crosscutting.middleware.core.MiddlewareChain;

@SuppressWarnings("rawtypes")
public class TenantMiddleware implements Middleware<HttpRequest, HttpResponse> {
    @Override
    public HttpResponse invoke(HttpRequest request, MiddlewareChain<HttpRequest, HttpResponse> chain) throws Exception {
        String tenantId = request.headers().firstValue("X-Tenant-ID").orElse("default");
        TenantContext.setTenant(tenantId);
        return chain.next(request);
    }
}

