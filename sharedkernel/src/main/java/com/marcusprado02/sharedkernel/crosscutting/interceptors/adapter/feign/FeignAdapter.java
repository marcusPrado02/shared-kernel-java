package com.marcusprado02.sharedkernel.crosscutting.interceptors.adapter.feign;

import java.util.List;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.DefaultInterceptorChain;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.HttpClientCtx;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Interceptor;

public class FeignAdapter implements RequestInterceptor {
    private final List<Interceptor<HttpClientCtx>> interceptors;
    public FeignAdapter(List<Interceptor<HttpClientCtx>> interceptors) { this.interceptors = interceptors; }

    @Override public void apply(RequestTemplate template) {
        var ctx = HttpClientCtx.from(template);
        var chain = new DefaultInterceptorChain<>(interceptors, c -> { c.execute(); return null; });
        try { chain.proceed(ctx); } catch (Exception e) { throw new RuntimeException(e); }
    }
}
