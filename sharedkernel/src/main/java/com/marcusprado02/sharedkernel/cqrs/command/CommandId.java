package com.marcusprado02.sharedkernel.cqrs.command;


import java.util.Objects;
import java.util.UUID;

/** Identificador forte do comando (pode ser ULID, aqui: UUID). */
public record CommandId(String value) {
    public CommandId {
        Objects.requireNonNull(value, "commandId");
        if (value.isBlank()) throw new IllegalArgumentException("commandId em branco");
    }
    public static CommandId newRandom() { return new CommandId(UUID.randomUUID().toString()); }
}
