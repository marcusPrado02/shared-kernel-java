package com.marcusprado02.sharedkernel.crosscutting.middleware.core;

import java.util.List;
import java.util.function.Function;

public class MiddlewarePipeline<T, R> {
    private final List<Middleware<T, R>> middlewares;
    private final Function<T, R> terminalHandler;

    public MiddlewarePipeline(List<Middleware<T, R>> middlewares,
                              Function<T, R> terminalHandler) {
        this.middlewares = List.copyOf(middlewares);
        this.terminalHandler = terminalHandler;
    }

    public R execute(T request) throws Exception {
        return new MiddlewareChainImpl<>(middlewares, terminalHandler, 0)
                .next(request);
    }

    private static class MiddlewareChainImpl<T, R> implements MiddlewareChain<T, R> {
        private final List<Middleware<T, R>> middlewares;
        private final Function<T, R> terminalHandler;
        private final int index;

        MiddlewareChainImpl(List<Middleware<T, R>> middlewares,
                            Function<T, R> terminalHandler,
                            int index) {
            this.middlewares = middlewares;
            this.terminalHandler = terminalHandler;
            this.index = index;
        }

        @Override
        public R next(T request) throws Exception {
            if (index < middlewares.size()) {
                Middleware<T, R> current = middlewares.get(index);
                return current.invoke(request,
                        new MiddlewareChainImpl<>(middlewares, terminalHandler, index + 1));
            } else {
                return terminalHandler.apply(request);
            }
        }
    }
}
