package com.marcusprado02.sharedkernel.infrastructure.messaging;


/** Opções de consumo. */
public record ConsumerOptions(
    String consumerGroup,
    int maxConcurrency,
    int prefetch,                 // Rabbit/NATS/SQS
    int maxPollRecords,           // Kafka
    int visibilityTimeoutSeconds, // SQS
    RetryPolicy retryPolicy,
    DlqPolicy dlqPolicy,
    boolean autoCommit
) {
  public static ConsumerOptions defaults(String group) {
    return new ConsumerOptions(group, 8, 256, 500, 60,
        RetryPolicy.exponential(), DlqPolicy.defaultDlq(), false);
  }
}
