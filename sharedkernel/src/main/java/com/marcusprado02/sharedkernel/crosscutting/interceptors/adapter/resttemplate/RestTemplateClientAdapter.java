package com.marcusprado02.sharedkernel.crosscutting.interceptors.adapter.resttemplate;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.DefaultInterceptorChain;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.HttpClientCtx;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Interceptor;

public class RestTemplateClientAdapter implements ClientHttpRequestInterceptor {
    private final List<Interceptor<HttpClientCtx>> interceptors;

    public RestTemplateClientAdapter(List<Interceptor<HttpClientCtx>> interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest req, byte[] body, ClientHttpRequestExecution ex) throws IOException {
        HttpClientCtx ctx = HttpClientCtx.from(req, body, ex);
        var chain = new DefaultInterceptorChain<>(interceptors, c -> {
            try { return (ClientHttpResponse) c.execute(); }
            catch (Exception e) { if (e instanceof IOException io) throw io; throw new IOException(e); }
        });
        try {
            return (ClientHttpResponse) chain.proceed(ctx);
        } catch (Exception e) {
            if (e instanceof IOException io) throw io;
            throw new IOException(e);
        }
    }
}
