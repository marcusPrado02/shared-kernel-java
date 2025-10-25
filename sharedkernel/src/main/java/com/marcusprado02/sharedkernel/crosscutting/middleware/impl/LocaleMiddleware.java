package com.marcusprado02.sharedkernel.crosscutting.middleware.impl;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.marcusprado02.sharedkernel.crosscutting.context.LocaleContext;
import com.marcusprado02.sharedkernel.crosscutting.middleware.core.*;

@SuppressWarnings("rawtypes")
public class LocaleMiddleware implements Middleware<HttpRequest, HttpResponse> {
    @Override
    public HttpResponse invoke(HttpRequest request, MiddlewareChain<HttpRequest, HttpResponse> chain) throws Exception {
        String locale = request.headers().firstValue("Accept-Language").orElse("en-US");
        LocaleContext.setLocale(locale);
        return chain.next(request);
    }
}
