package com.marcusprado02.sharedkernel.crosscutting.interceptors.core;

import java.util.List;

public final class DefaultInterceptorChain<TCtx extends InterceptionContext> implements InterceptorChain<TCtx> {
    private final List<Interceptor<TCtx>> interceptors;
    private final TerminalHandler<TCtx> terminal;
    private final int i;

    public interface TerminalHandler<TCtx> { Object handle(TCtx ctx) throws Exception; }

    public DefaultInterceptorChain(List<Interceptor<TCtx>> interceptors, TerminalHandler<TCtx> terminal) {
        this(interceptors, terminal, 0);
    }
    private DefaultInterceptorChain(List<Interceptor<TCtx>> list, TerminalHandler<TCtx> term, int idx) {
        this.interceptors = list; this.terminal = term; this.i = idx;
    }
    @Override public Object proceed(TCtx ctx) throws Exception {
        if (i < interceptors.size()) {
            return interceptors.get(i).around(ctx, new DefaultInterceptorChain<>(interceptors, terminal, i+1));
        }
        return terminal.handle(ctx);
    }
    @Override public int index() { return i; }
}
