package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

/** Revisões esperadas para concorrência otimista. */
public sealed interface ExpectedRevision permits ExpectedRevision.Any, ExpectedRevision.NoStream, ExpectedRevision.StreamExists, ExpectedRevision.Exact {
  record Any() implements ExpectedRevision {}
  record NoStream() implements ExpectedRevision {}
  record StreamExists() implements ExpectedRevision {}
  record Exact(long revision) implements ExpectedRevision {}
}
