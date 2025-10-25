package com.marcusprado02.sharedkernel.crosscutting.hook;

import java.time.Duration;
import java.util.Optional;

/** Resultado de uma execução de hook/callback. */
public record HookResult(
    boolean success,
    Duration duration,
    Optional<Throwable> error
) {}