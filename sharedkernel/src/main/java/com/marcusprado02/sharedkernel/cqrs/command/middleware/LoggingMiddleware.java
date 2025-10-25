package com.marcusprado02.sharedkernel.cqrs.command.middleware;

import java.time.Duration;

import com.marcusprado02.sharedkernel.cqrs.command.*;

public final class LoggingMiddleware implements CommandMiddleware {
    public interface Logger { void info(String msg); void warn(String msg); void error(String msg, Throwable t); }
    private final Logger log;
    public LoggingMiddleware(Logger log){ this.log = log; }

    @Override public <R> java.util.concurrent.CompletionStage<CommandResult<R>> invoke(CommandEnvelope<R> env, Next next) {
        var start = System.nanoTime();
        log.info("Dispatch " + env.command().getClass().getSimpleName() + " cmdId=" + env.metadata().commandId().value());
        return next.invoke(env).whenComplete((res, t) -> {
            var took = Duration.ofNanos(System.nanoTime()-start).toMillis();
            if (t != null) log.error("FAIL " + env.metadata().commandId().value() + " in " + took + "ms", t);
            else log.info("Done " + env.metadata().commandId().value() + " status=" + res.status() + " in " + took + "ms");
        });
    }
}
