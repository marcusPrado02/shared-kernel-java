package com.marcusprado02.sharedkernel.application.command;

import java.util.Optional;

/**
 * Interface para comandos que podem ser tratados de forma idempotente.
 */
public interface IdempotentCommand<R> extends Command<R> {

    /**
     * Token de idempotência único para prevenir múltiplas execuções do mesmo comando.
     */
    Optional<String> idempotencyKey();
}
