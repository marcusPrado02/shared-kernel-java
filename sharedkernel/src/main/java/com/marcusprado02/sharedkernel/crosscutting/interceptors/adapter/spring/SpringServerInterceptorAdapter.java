package com.marcusprado02.sharedkernel.crosscutting.interceptors.adapter.spring;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.marcusprado02.sharedkernel.crosscutting.context.HttpServerCtx;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.DefaultInterceptorChain;
import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SpringServerInterceptorAdapter implements HandlerInterceptor {
    private final List<Interceptor<HttpServerCtx>> interceptors;
    private final DefaultInterceptorChain.TerminalHandler<HttpServerCtx> terminal;

    public SpringServerInterceptorAdapter(List<Interceptor<HttpServerCtx>> interceptors) {
        this.interceptors = interceptors;
        this.terminal = ctx -> ctx.invokeHandler(); // chama controller real
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        HttpServerCtx ctx = HttpServerCtx.from(req, res, handler);
        var chain = new DefaultInterceptorChain<>(interceptors, terminal);
        Object result = chain.proceed(ctx);
        return ctx.shouldContinue(result);
    }
}
