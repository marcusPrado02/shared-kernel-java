package com.marcusprado02.sharedkernel.cqrs.query;

import java.util.Objects;

/** Envelope padrão: payload + metadados. */
public record QueryEnvelope<R>(Query<R> query, QueryMetadata metadata) {
    public QueryEnvelope {
        Objects.requireNonNull(query); Objects.requireNonNull(metadata);
    }
}
