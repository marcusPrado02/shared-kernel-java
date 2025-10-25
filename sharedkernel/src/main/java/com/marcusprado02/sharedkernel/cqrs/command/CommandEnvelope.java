package com.marcusprado02.sharedkernel.cqrs.command;

import java.util.Objects;

/** Envelope que carrega o payload (Command) + metadados. */
public record CommandEnvelope<R>(Command<R> command, CommandMetadata metadata) {
    public CommandEnvelope {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(metadata, "metadata");
    }
}
