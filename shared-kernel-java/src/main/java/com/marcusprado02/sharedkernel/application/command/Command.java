package com.marcusprado02.sharedkernel.application.command;

import java.io.Serializable;
import java.util.Optional;

/**
 * Interface base para comandos (mutações) no padrão CQRS.
 *
 * @param <R> Tipo do resultado esperado após a execução do comando.
 */
public interface Command<R> extends Serializable {

    /**
     * ID do comando (útil para rastreamento e idempotência).
     */
    default Optional<String> commandId() {
        return Optional.empty();
    }

    /**
     * Nome legível da classe do comando (útil para logs, auditoria, etc).
     */
    default String commandName() {
        return this.getClass().getSimpleName();
    }
}
