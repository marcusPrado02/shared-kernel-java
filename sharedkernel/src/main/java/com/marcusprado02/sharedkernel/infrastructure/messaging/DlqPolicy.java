package com.marcusprado02.sharedkernel.infrastructure.messaging;

/** Política de DLQ. */
public record DlqPolicy(
    String dlqTopic,                 // ex.: "<topic>.DLQ"
    String parkingLotTopic,          // ex.: "<topic>.PARK"
    boolean includeHeaders
) {
  public static DlqPolicy defaultDlq() { return new DlqPolicy("", "", true); }
}
