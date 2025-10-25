package com.marcusprado02.sharedkernel.infrastructure.inbox;

import java.util.Optional;

/** Políticas de processamento. */
public record ProcessingOptions(
    int maxAttempts,
    java.time.Duration baseBackoff,
    boolean validateSchema,
    Optional<String> schemaRef,
    boolean strictOrderingByKey,  // exige serialização por key no nível do consumer
    boolean requireTenant
) {
  public static ProcessingOptions defaults() {
    return new ProcessingOptions(12, java.time.Duration.ofSeconds(1), false, Optional.empty(), true, true);
  }
}
