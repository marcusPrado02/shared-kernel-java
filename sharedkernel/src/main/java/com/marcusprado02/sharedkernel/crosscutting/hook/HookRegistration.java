package com.marcusprado02.sharedkernel.crosscutting.hook;

import java.util.function.Predicate;

/** Metadados de registro. */
public record HookRegistration<E>(
    String topic,
    HookPhase phase,
    int priority,                       // menor = executa antes
    Predicate<E> filter,                // aplica somente se true
    Hook<E> handler,
    ExecutionPolicy policy              // retry/timeout/isolamento
) {}
