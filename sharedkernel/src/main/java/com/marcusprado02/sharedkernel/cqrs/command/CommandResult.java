package com.marcusprado02.sharedkernel.cqrs.command;

import java.util.List;
import java.util.Optional;

/** Resultado padronizado do processamento de um Command. */
public record CommandResult<R>(Status status, Optional<R> value, Optional<Throwable> error, List<String> warnings) {
    public enum Status { ACCEPTED, COMPLETED, REJECTED, FAILED, RETRY_SCHEDULED }
    public static <R> CommandResult<R> completed(R v) { return new CommandResult<>(Status.COMPLETED, Optional.ofNullable(v), Optional.empty(), List.of()); }
    public static <R> CommandResult<R> accepted(){ return new CommandResult<>(Status.ACCEPTED, Optional.empty(), Optional.empty(), List.of()); }
    public static <R> CommandResult<R> rejected(String reason){ return new CommandResult<>(Status.REJECTED, Optional.empty(), Optional.of(new IllegalStateException(reason)), List.of()); }
    public static <R> CommandResult<R> failed(Throwable t){ return new CommandResult<>(Status.FAILED, Optional.empty(), Optional.of(t), List.of()); }
    public static <R> CommandResult<R> retry(){ return new CommandResult<>(Status.RETRY_SCHEDULED, Optional.empty(), Optional.empty(), List.of()); }
}
