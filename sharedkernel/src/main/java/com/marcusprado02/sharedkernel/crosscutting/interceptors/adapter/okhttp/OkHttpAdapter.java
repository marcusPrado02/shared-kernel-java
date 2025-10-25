package com.marcusprado02.sharedkernel.crosscutting.interceptors.adapter.okhttp;

import java.io.IOException;
import java.util.List;

import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.DefaultInterceptorChain;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.HttpClientCtx;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Interceptor;

public final class OkHttpAdapter implements okhttp3.Interceptor {
    private final List<Interceptor<HttpClientCtx>> interceptors;
    public OkHttpAdapter(List<Interceptor<HttpClientCtx>> interceptors) { this.interceptors = interceptors; }

    @Override public okhttp3.Response intercept(Chain chain) throws IOException {
        var ctx = HttpClientCtx.from(chain.request(), chain);
        var defaultChain = new DefaultInterceptorChain<>(interceptors, c -> {
            try { return (okhttp3.Response) c.execute(); }
            catch (Exception e) { if (e instanceof IOException io) throw io; throw new IOException(e); }
        });
        try {
            return (okhttp3.Response) defaultChain.proceed(ctx);
        } catch (Exception e) {
            if (e instanceof IOException io) throw io;
            throw new IOException(e);
        }
    }
}
