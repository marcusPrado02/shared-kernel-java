package com.marcusprado02.sharedkernel.application.command;

import java.time.Instant;
import java.util.Optional;

/**
 * Interface para comandos que carregam metadados adicionais.
 */
public interface CommandWithMetadata<R> extends Command<R> {

    Optional<Instant> getTimestamp();

    Optional<String> getRequestedBy();

    Optional<String> getCorrelationId();

    Optional<String> getTenantId();
}
