package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;

import java.util.List;

import com.marcusprado02.sharedkernel.cqrs.command.*;

public interface PendingOutboxPicker {
  List<OutboxMsg> collect(Object command, CommandContext ctx);
  record OutboxMsg(String category, String key, Object payload) {}
}