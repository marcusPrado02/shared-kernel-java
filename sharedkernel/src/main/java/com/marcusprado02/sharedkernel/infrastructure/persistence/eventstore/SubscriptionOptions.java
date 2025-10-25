package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import java.util.Optional;

/** Config da assinatura. */
public record SubscriptionOptions(
    String subscriptionId,
    GlobalPosition fromPosition,
    int batchSize,
    boolean durable,
    boolean autoAck,
    Optional<String> consumerGroup
) {
  public static SubscriptionOptions ephemeralFromStart(String id) {
    return new SubscriptionOptions(id, GlobalPosition.START, 512, false, true, Optional.empty());
  }
}
