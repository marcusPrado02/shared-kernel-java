package com.marcusprado02.sharedkernel.cqrs.bus;

import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;

public record EventContext(Optional<String> correlationId, Optional<String> tenantId, Map<String,Object> headers) {
  public static EventContext from(CommandContext c) {
    return new EventContext(c.correlationId(), c.tenantId(), c.headers());
  }
}

